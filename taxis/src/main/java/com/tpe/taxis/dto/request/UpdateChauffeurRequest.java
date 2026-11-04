package com.tpe.taxis.dto.request;

import com.tpe.taxis.entity.ChauffeurStatut;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record UpdateChauffeurRequest(
        @Size(min = 2, max = 50, message = "Le nom doit comporter entre 2 et 50 caractères")
        String nom,

        @Size(min = 2, max = 50, message = "Le prénom doit comporter entre 2 et 50 caractères")
        String prenom,

        @Pattern(regexp = "^(\\+?\\d{1,4}[\\s-]?)?\\d{6,14}$", message = "Format de numéro de téléphone invalide")
        String telephone,

        @Size(min = 3, max = 30, message = "Le numéro de permis doit comporter entre 3 et 30 caractères")
        String numeroPermis,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateExpirationPermis,

        ChauffeurStatut statut,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateEmbauche,

        @Positive(message = "L'identifiant du véhicule doit être positif")
        Long vehiculeId
) {
}
