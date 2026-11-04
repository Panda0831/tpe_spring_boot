package com.tpe.taxis.dto.request;

import jakarta.validation.constraints.Positive;

public record AssignCourseRequest(
        @Positive(message = "L'identifiant du chauffeur doit être positif")
        Long chauffeurId,

        @Positive(message = "L'identifiant du véhicule doit être positif")
        Long vehiculeId
) {
}
