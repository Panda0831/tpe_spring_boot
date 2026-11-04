package com.tpe.parking.mapper;

import com.tpe.parking.dto.request.CreateAbonnementRequest;
import com.tpe.parking.dto.response.AbonnementResponse;
import com.tpe.parking.entity.Abonnement;
import com.tpe.parking.entity.AbonnementStatut;
import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.entity.Vehicule;

public final class AbonnementMapper {

    private AbonnementMapper() {
    }

    public static AbonnementResponse toResponse(Abonnement abonnement) {
        if (abonnement == null) {
            return null;
        }
        return AbonnementResponse.fromEntity(abonnement);
    }

    public static Abonnement toEntity(CreateAbonnementRequest request, Utilisateur utilisateur, Vehicule vehicule) {
        if (request == null) {
            return null;
        }
        Abonnement abonnement = new Abonnement();
        abonnement.setUtilisateur(utilisateur);
        abonnement.setVehicule(vehicule);
        abonnement.setType(request.type());
        abonnement.setDateDebut(request.dateDebut());

        if (request.dateFin() != null) {
            abonnement.setDateFin(request.dateFin());
        } else if (request.dateDebut() != null && request.type() != null) {
            switch (request.type()) {
                case JOURNALIER -> abonnement.setDateFin(request.dateDebut());
                case HEBDOMADAIRE -> abonnement.setDateFin(request.dateDebut().plusWeeks(1));
                case MENSUEL -> abonnement.setDateFin(request.dateDebut().plusMonths(1));
                case ANNUEL -> abonnement.setDateFin(request.dateDebut().plusYears(1));
            }
        }

        abonnement.setStatut(AbonnementStatut.ACTIF);
        return abonnement;
    }
}
