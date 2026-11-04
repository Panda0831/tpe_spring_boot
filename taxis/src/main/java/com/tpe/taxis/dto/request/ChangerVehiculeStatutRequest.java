package com.tpe.taxis.dto.request;

import com.tpe.taxis.entity.VehiculeStatut;
import jakarta.validation.constraints.NotNull;

public record ChangerVehiculeStatutRequest(
        @NotNull(message = "Le statut du véhicule est obligatoire")
        VehiculeStatut statut
) {
}
