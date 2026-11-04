package com.tpe.taxis.dto.response;

import com.tpe.taxis.entity.Vehicule;
import com.tpe.taxis.entity.VehiculeStatut;

public record VehiculeResponse(
        Long id,
        String immatriculation,
        String marque,
        String modele,
        Integer annee,
        Integer capacite,
        Double kilometrage,
        VehiculeStatut statut,
        Long chauffeurId,
        String nomChauffeur
) {
    public static VehiculeResponse fromEntity(Vehicule vehicule) {
        if (vehicule == null) {
            return null;
        }

        Long chauffeurId = null;
        String nomChauffeur = null;
        if (vehicule.getChauffeurActuel() != null) {
            chauffeurId = vehicule.getChauffeurActuel().getId();
            String prenom = vehicule.getChauffeurActuel().getPrenom() != null ? vehicule.getChauffeurActuel().getPrenom() : "";
            String nom = vehicule.getChauffeurActuel().getNom() != null ? vehicule.getChauffeurActuel().getNom() : "";
            nomChauffeur = (prenom + " " + nom).trim();
        }

        return new VehiculeResponse(
                vehicule.getId(),
                vehicule.getImmatriculation(),
                vehicule.getMarque(),
                vehicule.getModele(),
                vehicule.getAnnee(),
                vehicule.getCapacite(),
                vehicule.getKilometrage(),
                vehicule.getStatut(),
                chauffeurId,
                nomChauffeur
        );
    }
}
