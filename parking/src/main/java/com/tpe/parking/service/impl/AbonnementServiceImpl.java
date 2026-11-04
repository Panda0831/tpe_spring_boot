package com.tpe.parking.service.impl;

import java.time.LocalDate;
import java.util.List;

import com.tpe.parking.dto.request.CreateAbonnementRequest;
import com.tpe.parking.dto.response.AbonnementResponse;
import com.tpe.parking.entity.Abonnement;
import com.tpe.parking.entity.AbonnementStatut;
import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.entity.Vehicule;
import com.tpe.parking.exception.BusinessException;
import com.tpe.parking.exception.ConflictException;
import com.tpe.parking.exception.ResourceNotFoundException;
import com.tpe.parking.mapper.AbonnementMapper;
import com.tpe.parking.repository.AbonnementRepository;
import com.tpe.parking.repository.UtilisateurRepository;
import com.tpe.parking.repository.VehiculeRepository;
import com.tpe.parking.service.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AbonnementServiceImpl implements AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VehiculeRepository vehiculeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AbonnementResponse> getAllAbonnements() {
        return abonnementRepository.findAll()
                .stream()
                .map(AbonnementMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbonnementResponse> rechercherAbonnements(AbonnementStatut statut,
                                                          Long utilisateurId,
                                                          Long vehiculeId,
                                                          Pageable pageable) {
        return abonnementRepository.rechercher(statut, utilisateurId, vehiculeId, pageable)
                .map(AbonnementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AbonnementResponse getAbonnementById(Long id) {
        return abonnementRepository.findById(id)
                .map(AbonnementMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement non trouvé avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbonnementResponse> getAbonnementsByUtilisateur(Long utilisateurId) {
        return abonnementRepository.findByUtilisateurId(utilisateurId)
                .stream()
                .map(AbonnementMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbonnementResponse> getAbonnementsByVehicule(Long vehiculeId) {
        return abonnementRepository.findByVehiculeId(vehiculeId)
                .stream()
                .map(AbonnementMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbonnementResponse> getAbonnementsByStatut(AbonnementStatut statut) {
        return abonnementRepository.findByStatut(statut)
                .stream()
                .map(AbonnementMapper::toResponse)
                .toList();
    }

    @Override
    public AbonnementResponse souscrireAbonnement(CreateAbonnementRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(request.utilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + request.utilisateurId()));

        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + request.vehiculeId()));

        // Vérifier si le véhicule a déjà un abonnement actif pour la date de début
        if (abonnementRepository.findAbonnementActifParVehicule(vehicule.getId(), request.dateDebut()).isPresent()) {
            throw new ConflictException("Le véhicule possède déjà un abonnement actif pour cette période.");
        }

        Abonnement abonnement = AbonnementMapper.toEntity(request, utilisateur, vehicule);
        Abonnement saved = abonnementRepository.save(abonnement);
        return AbonnementMapper.toResponse(saved);
    }

    @Override
    public AbonnementResponse renouvelerAbonnement(Long id, LocalDate nouvelleDateFin) {
        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement non trouvé avec l'id : " + id));

        if (nouvelleDateFin != null) {
            if (abonnement.getDateFin() != null && !nouvelleDateFin.isAfter(abonnement.getDateFin())) {
                throw new BusinessException("La nouvelle date de fin doit être postérieure à la date de fin actuelle (" + abonnement.getDateFin() + ")");
            }
            abonnement.setDateFin(nouvelleDateFin);
        } else {
            // Prolongation automatique selon le type d'abonnement
            LocalDate baseDate = (abonnement.getDateFin() != null && abonnement.getDateFin().isAfter(LocalDate.now()))
                    ? abonnement.getDateFin()
                    : LocalDate.now();

            LocalDate calculatedFin = switch (abonnement.getType()) {
                case JOURNALIER -> baseDate.plusDays(1);
                case HEBDOMADAIRE -> baseDate.plusWeeks(1);
                case MENSUEL -> baseDate.plusMonths(1);
                case ANNUEL -> baseDate.plusYears(1);
            };
            abonnement.setDateFin(calculatedFin);
        }

        abonnement.setStatut(AbonnementStatut.ACTIF);
        Abonnement saved = abonnementRepository.save(abonnement);
        return AbonnementMapper.toResponse(saved);
    }

    @Override
    public void resilierAbonnement(Long id) {
        Abonnement abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement non trouvé avec l'id : " + id));

        abonnement.setStatut(AbonnementStatut.ANNULE);
        abonnementRepository.save(abonnement);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estAbonneActif(String immatriculation) {
        if (immatriculation == null || immatriculation.isBlank()) {
            return false;
        }
        return abonnementRepository.findAbonnementActifParImmatriculation(immatriculation.trim().toUpperCase(), LocalDate.now())
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estAbonneActif(Long vehiculeId) {
        if (vehiculeId == null) {
            return false;
        }
        return abonnementRepository.findAbonnementActifParVehicule(vehiculeId, LocalDate.now())
                .isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveAbonnement(Long utilisateurId) {
        if (utilisateurId == null) {
            return false;
        }
        List<Abonnement> abonnements = abonnementRepository.findByUtilisateurId(utilisateurId);
        return abonnements.stream()
                .anyMatch(a -> a.getStatut() == AbonnementStatut.ACTIF &&
                        a.getDateFin() != null &&
                        a.getDateFin().isAfter(LocalDate.now()));
    }
}
