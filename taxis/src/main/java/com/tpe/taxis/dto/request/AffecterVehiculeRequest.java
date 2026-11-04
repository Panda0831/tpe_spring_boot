package com.tpe.taxis.dto.request;

import jakarta.validation.constraints.Positive;

public record AffecterVehiculeRequest(
        @Positive(message = "L'identifiant du véhicule doit être positif")
        Long vehiculeId
) {
}
