package com.tpe.taxis.service;

import java.time.LocalDateTime;
import java.util.List;

import com.tpe.taxis.dto.request.AssignCourseRequest;
import com.tpe.taxis.dto.request.CreateCourseRequest;
import com.tpe.taxis.dto.response.CourseResponse;
import com.tpe.taxis.entity.CourseStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    CourseResponse createCourse(CreateCourseRequest request);

    CourseResponse getCourseById(Long id);

    List<CourseResponse> getAllCourses();

    Page<CourseResponse> getAllCourses(Pageable pageable);

    Page<CourseResponse> getCoursesByStatut(CourseStatut statut, Pageable pageable);

    Page<CourseResponse> getCoursesByClientId(Long clientId, Pageable pageable);

    Page<CourseResponse> getCoursesByChauffeurId(Long chauffeurId, Pageable pageable);

    Page<CourseResponse> getCoursesByVehiculeId(Long vehiculeId, Pageable pageable);

    Page<CourseResponse> getCoursesByPeriode(LocalDateTime debut, LocalDateTime fin, Pageable pageable);

    CourseResponse assignerCourse(Long courseId, AssignCourseRequest request);

    CourseResponse demarrerCourse(Long courseId);

    CourseResponse terminerCourse(Long courseId);

    CourseResponse annulerCourse(Long courseId);

    void deleteCourse(Long id);
}
