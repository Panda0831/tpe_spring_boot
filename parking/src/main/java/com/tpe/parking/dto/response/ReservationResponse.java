package com.tpe.parking.dto.response;

import java.time.LocalDateTime;

import com.tpe.parking.entity.Reservation;
import com.tpe.parking.entity.ReservationStatus;

public record ReservationResponse(
        Long id,
        Long utilisateurId,
        String nomUtilisateur,
        Long vehiculeId,
        String immatriculationVehicule,
        Long placeId,
        String numeroPlace,
        LocalDateTime dateArriveePrevue,
        LocalDateTime dateDepartPrevue,
        ReservationStatus statut
) {
    public static ReservationResponse fromEntity(Reservation reservation) {
        if (reservation == null) return null;

        String nomUtilisateur = null;
        if (reservation.getUtilisateur() != null) {
            String nom = reservation.getUtilisateur().getNom() != null ? reservation.getUtilisateur().getNom() : "";
            String prenom = reservation.getUtilisateur().getPrenom() != null ? reservation.getUtilisateur().getPrenom() : "";
            nomUtilisateur = (prenom + " " + nom).trim();
        }

        return new ReservationResponse(
                reservation.getId(),
                reservation.getUtilisateur() != null ? reservation.getUtilisateur().getId() : null,
                nomUtilisateur,
                reservation.getVehicule() != null ? reservation.getVehicule().getId() : null,
                reservation.getVehicule() != null ? reservation.getVehicule().getImmatriculation() : null,
                reservation.getPlace() != null ? reservation.getPlace().getId() : null,
                reservation.getPlace() != null ? reservation.getPlace().getNumero() : null,
                reservation.getDateArriveePrevue(),
                reservation.getDateDepartPrevue(),
                reservation.getStatut()
        );
    }
}
