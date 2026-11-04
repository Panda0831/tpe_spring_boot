package com.tpe.taxis.controller.rest;

import com.tpe.taxis.dto.request.AssignCourseRequest;
import com.tpe.taxis.dto.request.CreateCourseRequest;
import com.tpe.taxis.dto.response.CourseResponse;
import com.tpe.taxis.entity.CourseStatut;
import com.tpe.taxis.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseRestController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.created(URI.create("/api/courses/" + response.id())).body(response);
    }

    @GetMapping
    public Page<CourseResponse> getAllCourses(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return courseService.getAllCourses(pageable);
    }

    @GetMapping("/all")
    public List<CourseResponse> getAllCoursesList() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public CourseResponse getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @GetMapping("/statut/{statut}")
    public Page<CourseResponse> getCoursesByStatut(
            @PathVariable CourseStatut statut,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return courseService.getCoursesByStatut(statut, pageable);
    }

    @GetMapping("/client/{clientId}")
    public Page<CourseResponse> getCoursesByClient(
            @PathVariable Long clientId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return courseService.getCoursesByClientId(clientId, pageable);
    }

    @GetMapping("/chauffeur/{chauffeurId}")
    public Page<CourseResponse> getCoursesByChauffeur(
            @PathVariable Long chauffeurId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return courseService.getCoursesByChauffeurId(chauffeurId, pageable);
    }

    @GetMapping("/vehicule/{vehiculeId}")
    public Page<CourseResponse> getCoursesByVehicule(
            @PathVariable Long vehiculeId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return courseService.getCoursesByVehiculeId(vehiculeId, pageable);
    }

    @GetMapping("/periode")
    public Page<CourseResponse> getCoursesByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return courseService.getCoursesByPeriode(debut, fin, pageable);
    }

    @PostMapping("/{id}/assigner")
    public CourseResponse assignerCourse(@PathVariable Long id,
                                         @Valid @RequestBody AssignCourseRequest request) {
        return courseService.assignerCourse(id, request);
    }

    @PostMapping("/{id}/demarrer")
    public CourseResponse demarrerCourse(@PathVariable Long id) {
        return courseService.demarrerCourse(id);
    }

    @PostMapping("/{id}/terminer")
    public CourseResponse terminerCourse(@PathVariable Long id) {
        return courseService.terminerCourse(id);
    }

    @PostMapping("/{id}/annuler")
    public CourseResponse annulerCourse(@PathVariable Long id) {
        return courseService.annulerCourse(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
