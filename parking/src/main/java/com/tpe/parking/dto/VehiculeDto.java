package com.tpe.parking.dto;

import com.tpe.parking.entity.Vehicule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehiculeDto(
        Long id,

        @NotBlank(message = "Le numero d'immatriculation ne peut pas être vide")
        @Size(min = 2, max = 50, message = "Le numero d'immatriculation doit contenir entre 2 et 50 caractères")
        String immatriculation,

        String marque,

        @NotBlank(message = "Le modele ne peut pas être vide")
        @Size(min = 2, max = 50, message = "Le modele doit contenir entre 2 et 50 caractères")
        String modele,

        @NotNull(message = "L'utilisateur est obligatoire")
        UtilisateurDto utilisateur
) {
    public static VehiculeDto versDto(Vehicule vehicule) {
        if (vehicule == null) return null;

        return new VehiculeDto(
                vehicule.getId(),
                String.valueOf(vehicule.getImmatriculation()),
                vehicule.getMarque(),
                vehicule.getModele(),
                vehicule.getUtilisateur() != null ? UtilisateurDto.versDto(vehicule.getUtilisateur()) : null
        );
    }
}