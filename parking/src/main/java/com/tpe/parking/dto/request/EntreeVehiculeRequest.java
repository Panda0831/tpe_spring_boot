package com.tpe.parking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EntreeVehiculeRequest(
        @NotBlank(message = "L'immatriculation du véhicule est obligatoire")
        @Size(min = 2, max = 20, message = "L'immatriculation doit comporter entre 2 et 20 caractères")
        @Pattern(regexp = "[A-Za-z0-9 -]+", message = "L'immatriculation ne peut contenir que des lettres, chiffres, espaces et tirets")
        String immatriculation,

        @Positive(message = "L'ID de la place doit être positif")
        Long placeId, // Optionnel : si spécifié, place demandée ; sinon attribution automatique

        @Size(max = 50, message = "La zone ne peut pas dépasser 50 caractères")
        String zoneSouhaitee // Optionnel pour orienter l'attribution
) {}
