package com.tpe.parking.dto.response;

import com.tpe.parking.entity.Vehicule;

public record VehiculeResponse(
        Long id,
        String immatriculation,
        String marque,
        String modele,
        Long utilisateurId,
        String nomProprietaire
) {
    public static VehiculeResponse fromEntity(Vehicule vehicule) {
        if (vehicule == null) return null;

        String nomProprietaire = null;
        if (vehicule.getUtilisateur() != null) {
            String nom = vehicule.getUtilisateur().getNom() != null ? vehicule.getUtilisateur().getNom() : "";
            String prenom = vehicule.getUtilisateur().getPrenom() != null ? vehicule.getUtilisateur().getPrenom() : "";
            nomProprietaire = (prenom + " " + nom).trim();
        }

        return new VehiculeResponse(
                vehicule.getId(),
                vehicule.getImmatriculation(),
                vehicule.getMarque(),
                vehicule.getModele(),
                vehicule.getUtilisateur() != null ? vehicule.getUtilisateur().getId() : null,
                nomProprietaire
        );
    }
}
