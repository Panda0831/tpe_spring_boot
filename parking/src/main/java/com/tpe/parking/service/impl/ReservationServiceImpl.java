package com.tpe.parking.service.impl;

import com.tpe.parking.dto.request.CreateReservationRequest;
import com.tpe.parking.dto.request.UpdateReservationRequest;
import com.tpe.parking.dto.response.ReservationResponse;
import com.tpe.parking.entity.Place;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.Reservation;
import com.tpe.parking.entity.ReservationStatus;
import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.entity.Vehicule;
import com.tpe.parking.exception.BusinessException;
import com.tpe.parking.exception.ConflictException;
import com.tpe.parking.exception.ResourceNotFoundException;
import com.tpe.parking.repository.PlaceRepository;
import com.tpe.parking.repository.ReservationRepository;
import com.tpe.parking.repository.UtilisateurRepository;
import com.tpe.parking.repository.VehiculeRepository;
import com.tpe.parking.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PlaceRepository placeRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                 UtilisateurRepository utilisateurRepository,
                                 VehiculeRepository vehiculeRepository,
                                 PlaceRepository placeRepository) {
        this.reservationRepository = reservationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.placeRepository = placeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> rechercherReservations(ReservationStatus statut,
                                                            Long utilisateurId,
                                                            Long vehiculeId,
                                                            Long placeId,
                                                            Pageable pageable) {
        return reservationRepository.rechercher(statut, utilisateurId, vehiculeId, placeId, pageable)
                .map(ReservationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        return reservationRepository.findById(id)
                .map(ReservationResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByUtilisateur(Long utilisateurId) {
        return reservationRepository.findByUtilisateurId(utilisateurId).stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByVehicule(Long vehiculeId) {
        return reservationRepository.findByVehiculeId(vehiculeId).stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByPlace(Long placeId) {
        return reservationRepository.findByPlaceId(placeId).stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByStatut(ReservationStatus statut) {
        return reservationRepository.findByStatut(statut).stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    @Override
    public ReservationResponse createReservation(CreateReservationRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(request.utilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + request.utilisateurId()));

        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + request.vehiculeId()));

        if (vehicule.getUtilisateur() == null
                || !vehicule.getUtilisateur().getId().equals(utilisateur.getId())) {
            throw new BusinessException("Le véhicule sélectionné n'appartient pas à l'utilisateur choisi.");
        }

        if (request.dateArriveePrevue().isAfter(request.dateDepartPrevue())) {
            throw new BusinessException("La date d'arrivée prévue ne peut pas être après la date de départ prévue.");
        }

        Place place = resolvePlaceForReservation(request.placeId());
        if (place != null && reservationRepository.findReservationsChevauchantes(
                place.getId(), request.dateArriveePrevue(), request.dateDepartPrevue()).stream().findAny().isPresent()) {
            throw new ConflictException("Cette place est déjà réservée sur le créneau demandé.");
        }

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(utilisateur);
        reservation.setVehicule(vehicule);
        reservation.setPlace(place);
        reservation.setDateArriveePrevue(request.dateArriveePrevue());
        reservation.setDateDepartPrevue(request.dateDepartPrevue());
        reservation.setStatut(ReservationStatus.EN_ATTENTE);

        if (place != null) {
            place.setStatut(PlaceStatut.RESERVEE);
            placeRepository.save(place);
        }

        return ReservationResponse.fromEntity(reservationRepository.save(reservation));
    }

    @Override
    public ReservationResponse updateReservation(Long id, UpdateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec l'id : " + id));

        if (request.placeId() != null) {
            Place place = placeRepository.findById(request.placeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Place non trouvée avec l'id : " + request.placeId()));
            reservation.setPlace(place);
        }

        if (request.dateArriveePrevue() != null) {
            reservation.setDateArriveePrevue(request.dateArriveePrevue());
        }
        if (request.dateDepartPrevue() != null) {
            reservation.setDateDepartPrevue(request.dateDepartPrevue());
        }
        if (request.statut() != null) {
            reservation.setStatut(request.statut());
        }

        return ReservationResponse.fromEntity(reservationRepository.save(reservation));
    }

    @Override
    public void annulerReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec l'id : " + id));

        reservation.setStatut(ReservationStatus.ANNULEE);
        if (reservation.getPlace() != null) {
            reservation.getPlace().setStatut(PlaceStatut.LIBRE);
            placeRepository.save(reservation.getPlace());
        }
        reservationRepository.save(reservation);
    }

    @Override
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Réservation non trouvée avec l'id : " + id);
        }
        reservationRepository.deleteById(id);
    }

    private Place resolvePlaceForReservation(Long placeId) {
        if (placeId == null) {
            return placeRepository.findByStatut(PlaceStatut.LIBRE).stream().findFirst().orElse(null);
        }
        return placeRepository.findById(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("Place non trouvée avec l'id : " + placeId));
    }
}
