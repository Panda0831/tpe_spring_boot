package com.tpe.taxis.dto.request;

import com.tpe.taxis.entity.ChauffeurStatut;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record CreateChauffeurRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 50, message = "Le nom doit comporter entre 2 et 50 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(min = 2, max = 50, message = "Le prénom doit comporter entre 2 et 50 caractères")
        String prenom,

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^(\\+?\\d{1,4}[\\s-]?)?\\d{6,14}$", message = "Format de numéro de téléphone invalide")
        String telephone,

        @NotBlank(message = "Le numéro de permis est obligatoire")
        @Size(min = 3, max = 30, message = "Le numéro de permis doit comporter entre 3 et 30 caractères")
        String numeroPermis,

        @NotNull(message = "La date d'expiration du permis est obligatoire")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateExpirationPermis,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateEmbauche,

        ChauffeurStatut statut,

        @Positive(message = "L'identifiant du véhicule doit être positif")
        Long vehiculeId
) {
}
