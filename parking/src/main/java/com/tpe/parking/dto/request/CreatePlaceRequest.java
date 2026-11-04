package com.tpe.parking.dto.request;

import com.tpe.parking.entity.PlaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePlaceRequest(
        @NotBlank(message = "Le numéro de place est obligatoire")
        @Size(min = 1, max = 20, message = "Le numéro doit comporter entre 1 et 20 caractères")
        @Pattern(regexp = "[A-Za-z0-9-]+", message = "Le numéro accepte uniquement lettres, chiffres et tirets")
        String numero,

        @NotBlank(message = "La zone est obligatoire")
        @Size(min = 1, max = 50, message = "La zone doit comporter entre 1 et 50 caractères")
        String zone,

        @NotNull(message = "Le type de place est obligatoire (STANDARD, PMR, VIP, ABONNE)")
        PlaceType type
) {}
