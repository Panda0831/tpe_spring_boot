package com.tpe.taxis.dto.response;

import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.ChauffeurStatut;

import java.time.LocalDate;

public record ChauffeurResponse(
        Long id,
        String nom,
        String prenom,
        String nomComplet,
        String telephone,
        String numeroPermis,
        LocalDate dateExpirationPermis,
        boolean permisValide,
        ChauffeurStatut statut,
        LocalDate dateEmbauche,
        Long vehiculeId,
        String vehiculeImmatriculation,
        boolean eligiblePourCourse
) {
    public static ChauffeurResponse fromEntity(Chauffeur chauffeur) {
        if (chauffeur == null) {
            return null;
        }

        String nomComplet = ((chauffeur.getPrenom() != null ? chauffeur.getPrenom() : "") + " " +
                (chauffeur.getNom() != null ? chauffeur.getNom() : "")).trim();

        Long vehiculeId = null;
        String vehiculeImmatriculation = null;
        if (chauffeur.getVehicule() != null) {
            vehiculeId = chauffeur.getVehicule().getId();
            vehiculeImmatriculation = chauffeur.getVehicule().getImmatriculation();
        }

        return new ChauffeurResponse(
                chauffeur.getId(),
                chauffeur.getNom(),
                chauffeur.getPrenom(),
                nomComplet,
                chauffeur.getTelephone(),
                chauffeur.getNumeroPermis(),
                chauffeur.getDateExpirationPermis(),
                chauffeur.isPermisValide(),
                chauffeur.getStatut(),
                chauffeur.getDateEmbauche(),
                vehiculeId,
                vehiculeImmatriculation,
                chauffeur.isEligiblePourCourse()
        );
    }
}
