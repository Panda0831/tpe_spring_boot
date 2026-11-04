package com.tpe.taxis.service;

import com.tpe.taxis.dto.request.CreateCourseRequest;
import com.tpe.taxis.dto.response.CourseResponse;

public interface CourseDispatcherService {

    /**
     * Crée la course puis affecte automatiquement un chauffeur et un véhicule éligibles.
     */
    CourseResponse dispatcherCourse(CreateCourseRequest request);

    /**
     * Affecte automatiquement chauffeur + véhicule à une course existante en attente.
     */
    CourseResponse dispatcherCourse(Long courseId);

    /**
     * Affecte manuellement un chauffeur (et son véhicule) à une course en attente,
     * avec vérification des règles métier (statut, permis, conflit, maintenance).
     */
    CourseResponse assignerManuellement(Long courseId, Long chauffeurId, Long vehiculeId);
}
