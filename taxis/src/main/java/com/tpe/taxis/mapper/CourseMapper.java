package com.tpe.taxis.mapper;

import com.tpe.taxis.dto.request.CreateCourseRequest;
import com.tpe.taxis.dto.response.CourseResponse;
import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.Client;
import com.tpe.taxis.entity.Course;
import com.tpe.taxis.entity.CourseStatut;
import com.tpe.taxis.entity.Vehicule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class CourseMapper {

    private CourseMapper() {
    }

    public static CourseResponse toResponse(Course course) {
        if (course == null) {
            return null;
        }
        return CourseResponse.fromEntity(course);
    }

    public static Course toEntity(
            CreateCourseRequest request,
            Client client,
            Chauffeur chauffeur,
            Vehicule vehicule,
            BigDecimal tarif,
            BigDecimal commission,
            BigDecimal montantChauffeur,
            CourseStatut statut
    ) {
        if (request == null) {
            return null;
        }
        return Course.builder()
                .client(client)
                .pointDepart(request.pointDepart().trim())
                .destination(request.destination().trim())
                .distanceKm(request.distanceKm())
                .dateHeure(request.dateHeure() != null ? request.dateHeure() : LocalDateTime.now())
                .tarif(tarif != null ? tarif : BigDecimal.ZERO)
                .commission(commission != null ? commission : BigDecimal.ZERO)
                .montantChauffeur(montantChauffeur != null ? montantChauffeur : BigDecimal.ZERO)
                .chauffeur(chauffeur)
                .vehicule(vehicule)
                .statut(statut != null ? statut : CourseStatut.EN_ATTENTE)
                .build();
    }

    public static Course toEntity(
            CreateCourseRequest request,
            Client client,
            BigDecimal tarif,
            BigDecimal commission,
            BigDecimal montantChauffeur
    ) {
        return toEntity(request, client, null, null, tarif, commission, montantChauffeur, CourseStatut.EN_ATTENTE);
    }
}
