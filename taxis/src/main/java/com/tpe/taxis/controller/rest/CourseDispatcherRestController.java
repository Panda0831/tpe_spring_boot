package com.tpe.taxis.controller.rest;

import com.tpe.taxis.dto.request.AssignCourseRequest;
import com.tpe.taxis.dto.request.CreateCourseRequest;
import com.tpe.taxis.dto.response.CourseResponse;
import com.tpe.taxis.service.CourseDispatcherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class CourseDispatcherRestController {

    private final CourseDispatcherService courseDispatcherService;

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> dispatcherCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponse response = courseDispatcherService.dispatcherCourse(request);
        return ResponseEntity.created(URI.create("/api/courses/" + response.id())).body(response);
    }

    @PostMapping("/courses/{id}/dispatcher")
    public CourseResponse dispatcherCourseExistante(@PathVariable Long id) {
        return courseDispatcherService.dispatcherCourse(id);
    }

    @PostMapping("/courses/{id}/assigner")
    public CourseResponse assignerManuellement(@PathVariable Long id,
                                               @Valid @RequestBody AssignCourseRequest request) {
        return courseDispatcherService.assignerManuellement(
                id, request.chauffeurId(), request.vehiculeId());
    }
}
