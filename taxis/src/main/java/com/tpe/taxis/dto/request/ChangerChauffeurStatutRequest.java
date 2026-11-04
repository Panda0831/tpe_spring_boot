package com.tpe.taxis.dto.request;

import com.tpe.taxis.entity.ChauffeurStatut;
import jakarta.validation.constraints.NotNull;

public record ChangerChauffeurStatutRequest(
        @NotNull(message = "Le statut du chauffeur est obligatoire")
        ChauffeurStatut statut
) {
}
