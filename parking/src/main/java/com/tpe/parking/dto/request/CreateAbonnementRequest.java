package com.tpe.parking.dto.request;

import java.time.LocalDate;

import com.tpe.parking.entity.AbonnementType;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.AssertTrue;

public record CreateAbonnementRequest(
        @NotNull(message = "L'ID de l'utilisateur est obligatoire")
        @Positive(message = "L'ID de l'utilisateur doit être positif")
        Long utilisateurId,

        @NotNull(message = "L'ID du véhicule est obligatoire")
        @Positive(message = "L'ID du véhicule doit être positif")
        Long vehiculeId,

        @NotNull(message = "Le type d'abonnement est obligatoire (JOURNALIER, HEBDOMADAIRE, MENSUEL, ANNUEL)")
        AbonnementType type,

        @NotNull(message = "La date de début est obligatoire")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateDebut,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFin // Optionnel si calculé automatiquement selon le type
) {
    @AssertTrue(message = "La date de fin doit être postérieure à la date de début")
    public boolean isDateFinCoherente() {
        return dateFin == null || dateDebut == null || dateFin.isAfter(dateDebut);
    }
}
