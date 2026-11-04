package com.tpe.parking.controller.web;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.stream.Collectors;

final class FormValidation {

    private FormValidation() {
    }

    static String message(BindingResult result) {
        String validationMessages = result.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .filter(message -> message != null && !message.isBlank())
                .distinct()
                .collect(Collectors.joining(" "));
        return validationMessages.isBlank() ? "Veuillez vérifier les champs saisis." : validationMessages;
    }
}
