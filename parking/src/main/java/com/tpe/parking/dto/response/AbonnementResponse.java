package com.tpe.parking.dto.response;

import java.time.LocalDate;

import com.tpe.parking.entity.Abonnement;
import com.tpe.parking.entity.AbonnementStatut;
import com.tpe.parking.entity.AbonnementType;

public record AbonnementResponse(
        Long id,
        AbonnementType type,
        AbonnementStatut statut,
        LocalDate dateDebut,
        LocalDate dateFin,
        boolean estValide,
        Long utilisateurId,
        String nomUtilisateur,
        Long vehiculeId,
        String immatriculationVehicule
) {
    public static AbonnementResponse fromEntity(Abonnement abonnement) {
        if (abonnement == null) return null;

        String nomUtilisateur = null;
        if (abonnement.getUtilisateur() != null) {
            String nom = abonnement.getUtilisateur().getNom() != null ? abonnement.getUtilisateur().getNom() : "";
            String prenom = abonnement.getUtilisateur().getPrenom() != null ? abonnement.getUtilisateur().getPrenom() : "";
            nomUtilisateur = (prenom + " " + nom).trim();
        }

        return new AbonnementResponse(
                abonnement.getId(),
                abonnement.getType(),
                abonnement.getStatut(),
                abonnement.getDateDebut(),
                abonnement.getDateFin(),
                abonnement.estValide(),
                abonnement.getUtilisateur() != null ? abonnement.getUtilisateur().getId() : null,
                nomUtilisateur,
                abonnement.getVehicule() != null ? abonnement.getVehicule().getId() : null,
                abonnement.getVehicule() != null ? abonnement.getVehicule().getImmatriculation() : null
        );
    }
}
