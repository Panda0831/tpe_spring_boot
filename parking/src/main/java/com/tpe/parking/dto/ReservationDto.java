package com.tpe.parking.dto;

import java.time.LocalDateTime;
import com.tpe.parking.entity.Reservation;
import com.tpe.parking.entity.ReservationStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record ReservationDto(
        // Optionnel à la création (POST), renseigné en retour de réponse
        Long id,

        @NotNull(message = "L'ID de l'utilisateur est obligatoire")
        Long utilisateurId,

        @NotNull(message = "L'ID du véhicule est obligatoire")
        Long vehiculeId,

        @NotNull(message = "L'ID de la place est obligatoire")
        Long placeId,

        @NotNull(message = "La date d'arrivée est obligatoire")
        @FutureOrPresent(message = "La date d'arrivée ne peut pas être dans le passé")
        LocalDateTime dateArriveePrevue,

        @NotNull(message = "La date de départ est obligatoire")
        @Future(message = "La date de départ doit être dans le futur")
        LocalDateTime dateDepartPrevue,

        ReservationStatus statut
) {

    public static ReservationDto versDto(Reservation reservation) {
        if (reservation == null) return null;

        return new ReservationDto(
                reservation.getId(),
                reservation.getUtilisateur() != null ? reservation.getUtilisateur().getId() : null,
                reservation.getVehicule() != null ? reservation.getVehicule().getId() : null,
                reservation.getPlace() != null ? reservation.getPlace().getId() : null,
                reservation.getDateArriveePrevue(),
                reservation.getDateDepartPrevue(),
                reservation.getStatut()
        );
    }
}