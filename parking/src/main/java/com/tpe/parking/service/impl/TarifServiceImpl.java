package com.tpe.parking.service.impl;

import com.tpe.parking.dto.request.CreateTarifRequest;
import com.tpe.parking.dto.request.UpdateTarifRequest;
import com.tpe.parking.dto.response.TarifResponse;
import com.tpe.parking.entity.Role;
import com.tpe.parking.entity.Tarif;
import com.tpe.parking.exception.ConflictException;
import com.tpe.parking.exception.ResourceNotFoundException;
import com.tpe.parking.mapper.TarifMapper;
import com.tpe.parking.repository.TarifRepository;
import com.tpe.parking.service.TarifService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TarifServiceImpl implements TarifService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final TarifRepository tarifRepository;

    public TarifServiceImpl(TarifRepository tarifRepository) {
        this.tarifRepository = tarifRepository;
    }

    @Override
    public long getNombreTotalTarifs() {
        return tarifRepository.count();
    }

    @Override
    public List<Tarif> getAllTarifs() {
        return tarifRepository.findAll();
    }

    @Override
    public Tarif getTarifApplicable(String zone) {
        if (zone == null || zone.isBlank()) {
            return tarifRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("Aucun tarif configuré pour le parking."));
        }

        return tarifRepository.findFirstByZoneIgnoreCase(zone)
                .or(() -> tarifRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new IllegalStateException("Aucun tarif configuré pour la zone " + zone + "."));
    }

    @Override
    public TarifResponse getTarifById(Long id) {
        return TarifMapper.toResponse(tarifRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarif non trouvé avec l'id : " + id)));
    }

    @Override
    public TarifResponse creerTarif(CreateTarifRequest request) {
        if (request.zone() != null && tarifRepository.findFirstByZoneIgnoreCase(request.zone().trim()).isPresent()) {
            throw new ConflictException("Un tarif existe déjà pour la zone " + request.zone() + ".");
        }
        Tarif tarif = TarifMapper.toEntity(request);
        tarif.setZone(request.zone() == null ? null : request.zone().trim());
        return TarifMapper.toResponse(tarifRepository.save(tarif));
    }

    @Override
    public TarifResponse modifierTarif(Long id, UpdateTarifRequest request) {
        Tarif tarif = tarifRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarif non trouvé avec l'id : " + id));

        if (request.zone() != null && !request.zone().equalsIgnoreCase(tarif.getZone())
                && tarifRepository.findFirstByZoneIgnoreCase(request.zone().trim()).isPresent()) {
            throw new ConflictException("Un tarif existe déjà pour la zone " + request.zone() + ".");
        }

        TarifMapper.updateEntity(tarif, request);
        return TarifMapper.toResponse(tarifRepository.save(tarif));
    }

    @Override
    public void supprimerTarif(Long id) {
        if (!tarifRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarif non trouvé avec l'id : " + id);
        }
        tarifRepository.deleteById(id);
    }

    @Override
    public BigDecimal calculerMontant(LocalDateTime dateEntree, LocalDateTime dateSortie, String zone,
                                     boolean abonne, Role role, String typeVehicule) {
        if (dateEntree == null || dateSortie == null || !dateSortie.isAfter(dateEntree)) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        Tarif tarif = getTarifApplicable(zone);
        BigDecimal heures = BigDecimal.valueOf(Duration.between(dateEntree, dateSortie).toMinutes())
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        BigDecimal montant = calculerMontantBase(tarif, heures, abonne, role, typeVehicule);
        return normaliser(montant);
    }

    @Override
    public BigDecimal calculerMontantAvecDepassement(LocalDateTime dateEntree, LocalDateTime dateSortie,
                                                    LocalDateTime dateDepartPrevue, String zone,
                                                    boolean abonne, Role role, String typeVehicule) {
        if (dateEntree == null || dateSortie == null || !dateSortie.isAfter(dateEntree)) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        Tarif tarif = getTarifApplicable(zone);

        boolean enDepassement = dateDepartPrevue != null
                && dateDepartPrevue.isAfter(dateEntree)
                && dateSortie.isAfter(dateDepartPrevue);

        // En cas de dépassement, la base est facturée sur la durée *prévue* (sinon les heures
        // excédentaires seraient comptées deux fois) : 3h prévues + 2h de dépassement
        // => 3h au tarif normal puis 2h majorées. Sans dépassement, c'est la durée réelle qui compte.
        BigDecimal montantBase = calculerMontant(dateEntree, enDepassement ? dateDepartPrevue : dateSortie,
                zone, abonne, role, typeVehicule);

        if (!enDepassement) {
            return montantBase;
        }

        BigDecimal heuresSupplementaires = BigDecimal.valueOf(Duration.between(dateDepartPrevue, dateSortie).toMinutes())
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        if (heuresSupplementaires.compareTo(ZERO) <= 0) {
            return montantBase;
        }

        BigDecimal tarifHoraire = tarif.getMontantParHeure() == null ? ZERO : tarif.getMontantParHeure();
        if (abonne && tarif.getReductionAbonne() != null) {
            tarifHoraire = tarifHoraire.multiply(ONE.subtract(tarif.getReductionAbonne()));
        }

        if (tarifHoraire.compareTo(ZERO) <= 0) {
            return montantBase;
        }

        BigDecimal majoration = tarif.getMajorationApres() == null ? ZERO : tarif.getMajorationApres();
        BigDecimal tauxSupplementaire = tarifHoraire.multiply(ONE.add(majoration));
        BigDecimal depassement = tauxSupplementaire.multiply(heuresSupplementaires);

        BigDecimal total = montantBase.add(depassement);
        if (tarif.getTarifMinimum() != null && total.compareTo(tarif.getTarifMinimum()) < 0) {
            total = tarif.getTarifMinimum();
        }
        return normaliser(total);
    }

    private BigDecimal calculerMontantBase(Tarif tarif, BigDecimal heures, boolean abonne,
                                          Role role, String typeVehicule) {
        BigDecimal tarifHoraire = tarif.getMontantParHeure() == null ? ZERO : tarif.getMontantParHeure();
        if (abonne && tarif.getReductionAbonne() != null && tarif.getReductionAbonne().compareTo(ZERO) > 0) {
            tarifHoraire = tarifHoraire.multiply(ONE.subtract(tarif.getReductionAbonne()));
        }

        if (role != null && (role == Role.MANAGER || role == Role.ADMIN)) {
            tarifHoraire = tarifHoraire.multiply(BigDecimal.valueOf(0.90));
        }

        if (typeVehicule != null && typeVehicule.equalsIgnoreCase("VELO")) {
            tarifHoraire = tarifHoraire.multiply(BigDecimal.valueOf(0.70));
        }

        BigDecimal montant = tarifHoraire.multiply(heures);
        if (tarif.getTarifMinimum() != null && montant.compareTo(tarif.getTarifMinimum()) < 0) {
            montant = tarif.getTarifMinimum();
        }

        if (tarif.getForfaitJournalier() != null && heures.compareTo(BigDecimal.valueOf(24)) >= 0) {
            montant = montant.min(tarif.getForfaitJournalier());
        }

        return montant;
    }

    private BigDecimal normaliser(BigDecimal valeur) {
        if (valeur == null) {
            return ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return valeur.setScale(2, RoundingMode.HALF_UP);
    }
}
