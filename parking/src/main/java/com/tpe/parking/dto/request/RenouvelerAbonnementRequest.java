package com.tpe.parking.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import org.springframework.format.annotation.DateTimeFormat;

public record RenouvelerAbonnementRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @FutureOrPresent(message = "La nouvelle date de fin ne peut pas être dans le passé")
        LocalDate nouvelleDateFin
) {}
