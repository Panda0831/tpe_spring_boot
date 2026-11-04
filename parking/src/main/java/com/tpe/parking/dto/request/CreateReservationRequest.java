package com.tpe.parking.dto.request;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.AssertTrue;

public record CreateReservationRequest(
        @NotNull(message = "L'ID de l'utilisateur est obligatoire")
        @Positive(message = "L'ID de l'utilisateur doit être positif")
        Long utilisateurId,

        @NotNull(message = "L'ID du véhicule est obligatoire")
        @Positive(message = "L'ID du véhicule doit être positif")
        Long vehiculeId,

        @Positive(message = "L'ID de la place doit être positif")
        Long placeId, // Optionnel : si nul, le système attribue automatiquement une place

        @NotNull(message = "La date d'arrivée prévue est obligatoire")
        @FutureOrPresent(message = "La date d'arrivée ne peut pas être dans le passé")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateArriveePrevue,

        @NotNull(message = "La date de départ prévue est obligatoire")
        @Future(message = "La date de départ doit être dans le futur")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateDepartPrevue
) {
    @AssertTrue(message = "La date d'arrivée prévue doit être antérieure à la date de départ prévue")
    public boolean isPeriodeCoherente() {
        return dateArriveePrevue == null || dateDepartPrevue == null
                || !dateArriveePrevue.isAfter(dateDepartPrevue);
    }
}
