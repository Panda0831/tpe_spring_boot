package com.tpe.parking.dto.request;

import com.tpe.parking.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUtilisateurRequest(
        @NotBlank(message = "Le nom ne peut pas être vide")
        @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
        String nom,

        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
        String prenom,

        @NotBlank(message = "Le numéro de téléphone ne peut pas être vide")
        @Pattern(regexp = "(?:0[0-9]{9}|\\+?[1-9][0-9]{7,14})", message = "Saisissez un numéro local de 10 chiffres ou un numéro international de 8 à 15 chiffres")
        String telephone,

        @NotBlank(message = "L'adresse email ne peut pas être vide")
        @Email(message = "L'adresse email doit être valide")
        @Size(max = 254, message = "L'adresse email ne peut pas dépasser 254 caractères")
        String email,

        String motDePasse,

        Role role
) {}
