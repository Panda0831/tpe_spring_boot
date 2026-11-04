package com.tpe.parking.service;

import com.tpe.parking.dto.request.CreateReservationRequest;
import com.tpe.parking.dto.request.UpdateReservationRequest;
import com.tpe.parking.dto.response.ReservationResponse;
import com.tpe.parking.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReservationService {
    List<ReservationResponse> getAllReservations();

    /** Liste paginée, triée et filtrée des réservations (filtres tous optionnels). */
    Page<ReservationResponse> rechercherReservations(ReservationStatus statut,
                                                     Long utilisateurId,
                                                     Long vehiculeId,
                                                     Long placeId,
                                                     Pageable pageable);

    ReservationResponse getReservationById(Long id);

    List<ReservationResponse> getReservationsByUtilisateur(Long utilisateurId);

    List<ReservationResponse> getReservationsByVehicule(Long vehiculeId);

    List<ReservationResponse> getReservationsByPlace(Long placeId);

    List<ReservationResponse> getReservationsByStatut(ReservationStatus statut);

    ReservationResponse createReservation(CreateReservationRequest request);

    ReservationResponse updateReservation(Long id, UpdateReservationRequest request);

    void annulerReservation(Long id);

    void deleteReservation(Long id);
}
