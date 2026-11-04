package com.tpe.parking.service.impl;

import com.tpe.parking.dto.request.EntreeVehiculeRequest;
import com.tpe.parking.dto.request.SortieVehiculeRequest;
import com.tpe.parking.dto.response.EntreeSortieResponse;
import com.tpe.parking.entity.EntreeSortie;
import com.tpe.parking.entity.Place;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.PlaceType;
import com.tpe.parking.entity.Reservation;
import com.tpe.parking.entity.ReservationStatus;
import com.tpe.parking.entity.Role;
import com.tpe.parking.entity.Vehicule;
import com.tpe.parking.exception.InvalidOperationException;
import com.tpe.parking.exception.PlaceUnavailableException;
import com.tpe.parking.exception.ResourceNotFoundException;
import com.tpe.parking.repository.EntreeSortieRepository;
import com.tpe.parking.repository.PlaceRepository;
import com.tpe.parking.repository.ReservationRepository;
import com.tpe.parking.repository.VehiculeRepository;
import com.tpe.parking.service.AbonnementService;
import com.tpe.parking.service.EntreeSortieService;
import com.tpe.parking.service.TarifService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Gestion transactionnelle des passages à la borne.
 *
 * <p><b>Entrée :</b> résolution du véhicule par plaque, recherche d'une place compatible
 * (réservation existante, abonnement, zone souhaitée puis substitution), passage de la place
 * à {@code OCCUPEE} et création de l'historique.</p>
 *
 * <p><b>Sortie :</b> calcul de la durée et du montant (majoration de dépassement incluse),
 * libération de la place et clôture de l'historique.</p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class EntreeSortieServiceImpl implements EntreeSortieService {

    private final EntreeSortieRepository entreeSortieRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PlaceRepository placeRepository;
    private final ReservationRepository reservationRepository;
    private final AbonnementService abonnementService;
    private final TarifService tarifService;

    @Override
    public EntreeSortieResponse enregistrerEntree(EntreeVehiculeRequest request) {
        String immatriculation = normaliser(request.immatriculation());
        Vehicule vehicule = vehiculeRepository.findByImmatriculation(immatriculation)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun véhicule enregistré avec l'immatriculation " + immatriculation));

        if (entreeSortieRepository.findEntreeEnCoursParImmatriculation(immatriculation).isPresent()) {
            throw new InvalidOperationException(
                    "Le véhicule " + immatriculation + " est déjà présent dans le parking.");
        }

        LocalDateTime maintenant = LocalDateTime.now();
        Reservation reservation = trouverReservationActive(vehicule, maintenant);

        Place place;
        LocalDateTime departPrevu = null;
        if (request.placeId() != null) {
            place = resoudrePlaceDemandee(request.placeId());
        } else if (reservation != null && reservation.getPlace() != null
                && reservation.getPlace().getStatut() == PlaceStatut.LIBRE) {
            // Priorité à la place réservée par le client
            place = reservation.getPlace();
            departPrevu = reservation.getDateDepartPrevue();
        } else {
            place = choisirPlaceLibre(vehicule, request.zoneSouhaitee());
        }

        if (place == null) {
            throw new PlaceUnavailableException(
                    "Aucune place disponible pour le véhicule " + immatriculation + ".");
        }

        place.setStatut(PlaceStatut.OCCUPEE);
        placeRepository.save(place);

        EntreeSortie entree = new EntreeSortie();
        entree.setVehicule(vehicule);
        entree.setPlace(place);
        entree.setDateEntree(maintenant);
        entree.setDateDepartPrevue(departPrevu);

        return EntreeSortieResponse.fromEntity(entreeSortieRepository.save(entree));
    }

    @Override
    public EntreeSortieResponse enregistrerSortie(SortieVehiculeRequest request) {
        String immatriculation = normaliser(request.immatriculation());
        Vehicule vehicule = vehiculeRepository.findByImmatriculation(immatriculation)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun véhicule enregistré avec l'immatriculation " + immatriculation));

        EntreeSortie entree = entreeSortieRepository.findEntreeEnCoursParImmatriculation(immatriculation)
                .orElseThrow(() -> new InvalidOperationException(
                        "Aucune entrée en cours pour le véhicule " + immatriculation + "."));

        LocalDateTime sortie = LocalDateTime.now();
        Place place = entree.getPlace();
        boolean abonne = abonnementService.estAbonneActif(vehicule.getId());
        Role role = vehicule.getUtilisateur() != null ? vehicule.getUtilisateur().getRole() : null;
        String zone = place != null ? place.getZone() : null;

        BigDecimal montant = tarifService.calculerMontantAvecDepassement(
                entree.getDateEntree(), sortie, entree.getDateDepartPrevue(), zone, abonne, role, null);

        entree.setDateSortie(sortie);
        entree.setMontant(montant);
        entreeSortieRepository.save(entree);

        if (place != null) {
            place.setStatut(PlaceStatut.LIBRE);
            placeRepository.save(place);
        }

        return EntreeSortieResponse.fromEntity(entree);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntreeSortieResponse> getAllEntreesSorties() {
        return entreeSortieRepository.findAll().stream()
                .map(EntreeSortieResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EntreeSortieResponse> rechercherEntreesSorties(Boolean enCours,
                                                              String immatriculation,
                                                              Pageable pageable) {
        String plaque = immatriculation == null || immatriculation.isBlank()
                ? "" : immatriculation.trim();
        return entreeSortieRepository.rechercher(enCours, plaque, pageable)
                .map(EntreeSortieResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public EntreeSortieResponse getEntreeSortieById(Long id) {
        return entreeSortieRepository.findById(id)
                .map(EntreeSortieResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Entrée/sortie non trouvée avec l'id : " + id));
    }

    // ------------------------------------------------------------------
    // Aide à l'attribution automatique de place
    // ------------------------------------------------------------------

    private Place resoudrePlaceDemandee(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("Place non trouvée avec l'id : " + placeId));
        if (place.getStatut() != PlaceStatut.LIBRE) {
            throw new PlaceUnavailableException("La place " + place.getNumero() + " n'est pas libre.");
        }
        return place;
    }

    /** Réservation non annulée/terminée couvrant l'instant présent pour ce véhicule. */
    private Reservation trouverReservationActive(Vehicule vehicule, LocalDateTime instant) {
        return reservationRepository.findByVehiculeId(vehicule.getId()).stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE
                        || r.getStatut() == ReservationStatus.EN_ATTENTE)
                .filter(r -> r.getDateArriveePrevue() != null && r.getDateDepartPrevue() != null)
                .filter(r -> !instant.isBefore(r.getDateArriveePrevue())
                        && !instant.isAfter(r.getDateDepartPrevue()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Règle d'attribution avec substitution :
     * <ol>
     *   <li>type privilégié (ABONNE si abonné actif, sinon STANDARD) dans la zone souhaitée ;</li>
     *   <li>même type dans n'importe quelle zone ;</li>
     *   <li>n'importe quelle place libre (zone souhaitée privilégiée).</li>
     * </ol>
     * Les places PMR et VIP restent réservées aux demandes explicites.
     */
    private Place choisirPlaceLibre(Vehicule vehicule, String zoneSouhaitee) {
        List<Place> libres = placeRepository.findByStatut(PlaceStatut.LIBRE);
        if (libres.isEmpty()) {
            return null;
        }

        boolean abonne = abonnementService.estAbonneActif(vehicule.getId());
        List<PlaceType> priorite = abonne
                ? List.of(PlaceType.ABONNE, PlaceType.STANDARD)
                : List.of(PlaceType.STANDARD);

        boolean zoneFiltree = zoneSouhaitee != null && !zoneSouhaitee.isBlank();

        for (PlaceType type : priorite) {
            Optional<Place> candidat = libres.stream()
                    .filter(p -> p.getType() == type)
                    .filter(p -> !zoneFiltree || zoneSouhaitee.equalsIgnoreCase(p.getZone()))
                    .findFirst();
            if (candidat.isPresent()) {
                return candidat.get();
            }
        }

        for (PlaceType type : priorite) {
            Optional<Place> candidat = libres.stream()
                    .filter(p -> p.getType() == type)
                    .findFirst();
            if (candidat.isPresent()) {
                return candidat.get();
            }
        }

        return libres.stream()
                .filter(p -> !zoneFiltree || zoneSouhaitee.equalsIgnoreCase(p.getZone()))
                .findFirst()
                .orElse(libres.get(0));
    }

    private String normaliser(String immatriculation) {
        return immatriculation == null ? null : immatriculation.trim().toUpperCase();
    }
}
