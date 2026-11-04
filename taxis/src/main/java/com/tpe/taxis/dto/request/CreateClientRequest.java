package com.tpe.taxis.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @NotBlank(message = "Le nom du client est obligatoire")
        @Size(min = 2, max = 50, message = "Le nom doit comporter entre 2 et 50 caractères")
        String nom,

        @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
        String prenom,

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^(\\+?\\d{1,4}[\\s-]?)?\\d{6,14}$", message = "Format de numéro de téléphone invalide")
        String telephone,

        @Email(message = "Format d'adresse email invalide")
        String email
) {
}
