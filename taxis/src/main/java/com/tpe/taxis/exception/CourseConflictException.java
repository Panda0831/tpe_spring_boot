package com.tpe.taxis.exception;

public class CourseConflictException extends ConflictException {

    public CourseConflictException(String message) {
        super(message);
    }

    public CourseConflictException(Long courseId, String raison) {
        super(String.format("Conflit sur la course %d : %s", courseId, raison));
    }
}
