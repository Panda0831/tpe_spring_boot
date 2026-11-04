package com.tpe.parking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateVehiculeRequest(
        @NotBlank(message = "L'immatriculation est obligatoire")
        @Size(min = 2, max = 20, message = "L'immatriculation doit comporter entre 2 et 20 caractères")
        @Pattern(regexp = "[A-Za-z0-9 -]+", message = "L'immatriculation ne peut contenir que des lettres, chiffres, espaces et tirets")
        String immatriculation,

        @Size(max = 50, message = "La marque ne peut pas dépasser 50 caractères")
        String marque,

        @Size(max = 50, message = "Le modèle ne peut pas dépasser 50 caractères")
        String modele,

        @NotNull(message = "L'identifiant du propriétaire (utilisateur) est obligatoire")
        @Positive(message = "L'identifiant du propriétaire doit être positif")
        Long utilisateurId
) {}
