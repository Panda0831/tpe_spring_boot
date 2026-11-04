package com.tpe.parking.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdateTarifRequest(
        @Size(max = 50, message = "La zone ne peut pas dépasser 50 caractères")
        @Pattern(regexp = "(?s).*\\S.*", message = "La zone ne peut pas être vide")
        String zone,

        @DecimalMin(value = "0.0", inclusive = false, message = "Le montant par heure doit être supérieur à 0")
        BigDecimal montantParHeure,

        @PositiveOrZero(message = "Le tarif minimum doit être positif ou nul")
        BigDecimal tarifMinimum,

        @PositiveOrZero(message = "Le forfait journalier doit être positif ou nul")
        BigDecimal forfaitJournalier,

        @PositiveOrZero(message = "La réduction abonné doit être positive ou nulle")
        @DecimalMax(value = "1.0", message = "La réduction abonné ne peut pas dépasser 1")
        BigDecimal reductionAbonne,

        @PositiveOrZero(message = "La majoration après dépassement doit être positive ou nulle")
        BigDecimal majorationApres
) {}
