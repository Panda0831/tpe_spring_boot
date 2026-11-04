package com.tpe.parking.dto.request;

import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.PlaceType;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdatePlaceRequest(
        @Size(min = 1, max = 20, message = "Le numéro doit comporter entre 1 et 20 caractères")
        @Pattern(regexp = "(?s).*\\S.*", message = "Le numéro de place ne peut pas être vide")
        String numero,

        @Size(min = 1, max = 50, message = "La zone doit comporter entre 1 et 50 caractères")
        @Pattern(regexp = "(?s).*\\S.*", message = "La zone ne peut pas être vide")
        String zone,

        PlaceType type,
        PlaceStatut statut
) {}
