package com.tpe.taxis.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s introuvable avec l'identifiant : %d", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(String.format("%s introuvable avec %s : %s", resourceName, field, value));
    }
}
