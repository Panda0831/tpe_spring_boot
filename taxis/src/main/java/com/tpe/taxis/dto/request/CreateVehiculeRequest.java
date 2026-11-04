package com.tpe.taxis.dto.request;

import com.tpe.taxis.entity.VehiculeStatut;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateVehiculeRequest(
        @NotBlank(message = "L'immatriculation est obligatoire")
        @Size(min = 2, max = 20, message = "L'immatriculation doit comporter entre 2 et 20 caractères")
        String immatriculation,

        @NotBlank(message = "La marque est obligatoire")
        @Size(min = 1, max = 50, message = "La marque ne peut pas dépasser 50 caractères")
        String marque,

        @NotBlank(message = "Le modèle est obligatoire")
        @Size(min = 1, max = 50, message = "Le modèle ne peut pas dépasser 50 caractères")
        String modele,

        @Min(value = 1900, message = "L'année doit être supérieure ou égale à 1900")
        @Max(value = 2100, message = "L'année est invalide")
        Integer annee,

        @NotNull(message = "La capacité est obligatoire")
        @Min(value = 1, message = "La capacité minimale est de 1 passager")
        @Max(value = 20, message = "La capacité maximale est de 20 passagers")
        Integer capacite,

        @PositiveOrZero(message = "Le kilométrage doit être positif ou nul")
        Double kilometrage,

        VehiculeStatut statut,

        @Positive(message = "L'identifiant du chauffeur doit être positif")
        Long chauffeurId
) {
}
