package com.tpe.parking.dto.request;

import java.time.LocalDateTime;

import com.tpe.parking.entity.ReservationStatus;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.AssertTrue;

public record UpdateReservationRequest(
        @Positive(message = "L'ID de la place doit être positif")
        Long placeId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateArriveePrevue,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateDepartPrevue,
        ReservationStatus statut
) {
    @AssertTrue(message = "La date d'arrivée prévue doit être antérieure à la date de départ prévue")
    public boolean isPeriodeCoherente() {
        return dateArriveePrevue == null || dateDepartPrevue == null
                || !dateArriveePrevue.isAfter(dateDepartPrevue);
    }
}
