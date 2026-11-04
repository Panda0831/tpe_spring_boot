package com.tpe.taxis.dto.request;

import jakarta.validation.constraints.Positive;

public record AffecterChauffeurRequest(
        @Positive(message = "L'identifiant du chauffeur doit être positif")
        Long chauffeurId
) {
}
