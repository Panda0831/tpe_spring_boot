package com.tpe.taxis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record CreateCourseRequest(
        @NotNull(message = "L'identifiant du client est obligatoire")
        @Positive(message = "L'identifiant du client doit être positif")
        Long clientId,

        @NotBlank(message = "Le point de départ est obligatoire")
        @Size(max = 255, message = "Le point de départ ne peut pas dépasser 255 caractères")
        String pointDepart,

        @NotBlank(message = "La destination est obligatoire")
        @Size(max = 255, message = "La destination ne peut pas dépasser 255 caractères")
        String destination,

        @NotNull(message = "La distance en kilomètres est obligatoire")
        @Positive(message = "La distance doit être strictement positive")
        Double distanceKm,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dateHeure,

        @Positive(message = "L'identifiant du chauffeur doit être positif")
        Long chauffeurId,

        @Positive(message = "L'identifiant du véhicule doit être positif")
        Long vehiculeId
) {
}
