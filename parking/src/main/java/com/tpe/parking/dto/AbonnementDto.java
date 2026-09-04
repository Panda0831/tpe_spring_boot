package com.tpe.parking.dto;

import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.entity.Vehicule;
import jakarta.validation.constraints.NotNull;

public record AbonnementDto(
        Long id,
        @NotNull(message = "Le type d'abonnement est obligatoire")
        String type,
        @NotNull(message = "Le véhicule est obligatoire")
        Vehicule vehicule,
        String status,
        Utilisateur utilisateur,
        @NotNull(message = "La date de début est obligatoire")
        String dateDebut,
        @NotNull(message = "La date de fin est obligatoire")
        String dateFin
) {
    public static AbonnementDto versDto(com.tpe.parking.entity.Abonnement abonnement) {
        if (abonnement == null) return null;

        return new AbonnementDto(
                abonnement.getId(),
                abonnement.getType() != null ? abonnement.getType().name() : null,
                abonnement.getVehicule(),
                abonnement.getStatut() != null ? abonnement.getStatut().name() : null,
                abonnement.getUtilisateur(),
                abonnement.getDateDebut() != null ? abonnement.getDateDebut().toString() : null,
                abonnement.getDateFin() != null ? abonnement.getDateFin().toString() : null
        );
    }
 
}
