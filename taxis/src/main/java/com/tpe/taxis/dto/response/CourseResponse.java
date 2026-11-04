package com.tpe.taxis.dto.response;

import com.tpe.taxis.entity.Course;
import com.tpe.taxis.entity.CourseStatut;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourseResponse(
        Long id,
        Long clientId,
        String nomClient,
        String telephoneClient,
        String pointDepart,
        String destination,
        Double distanceKm,
        LocalDateTime dateHeure,
        BigDecimal tarif,
        BigDecimal commission,
        BigDecimal montantChauffeur,
        Long chauffeurId,
        String nomChauffeur,
        Long vehiculeId,
        String immatriculationVehicule,
        CourseStatut statut
) {
    public static CourseResponse fromEntity(Course course) {
        if (course == null) {
            return null;
        }

        Long clientId = null;
        String nomClient = null;
        String telephoneClient = null;
        if (course.getClient() != null) {
            clientId = course.getClient().getId();
            String prenom = course.getClient().getPrenom() != null ? course.getClient().getPrenom() : "";
            String nom = course.getClient().getNom() != null ? course.getClient().getNom() : "";
            nomClient = (prenom + " " + nom).trim();
            telephoneClient = course.getClient().getTelephone();
        }

        Long chauffeurId = null;
        String nomChauffeur = null;
        if (course.getChauffeur() != null) {
            chauffeurId = course.getChauffeur().getId();
            String prenom = course.getChauffeur().getPrenom() != null ? course.getChauffeur().getPrenom() : "";
            String nom = course.getChauffeur().getNom() != null ? course.getChauffeur().getNom() : "";
            nomChauffeur = (prenom + " " + nom).trim();
        }

        Long vehiculeId = null;
        String immatriculationVehicule = null;
        if (course.getVehicule() != null) {
            vehiculeId = course.getVehicule().getId();
            immatriculationVehicule = course.getVehicule().getImmatriculation();
        }

        return new CourseResponse(
                course.getId(),
                clientId,
                nomClient,
                telephoneClient,
                course.getPointDepart(),
                course.getDestination(),
                course.getDistanceKm(),
                course.getDateHeure(),
                course.getTarif(),
                course.getCommission(),
                course.getMontantChauffeur(),
                chauffeurId,
                nomChauffeur,
                vehiculeId,
                immatriculationVehicule,
                course.getStatut()
        );
    }
}
