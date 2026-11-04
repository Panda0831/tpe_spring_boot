package com.tpe.taxis.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateParametreTarifWebRequest(
        @NotNull(message = "L'identifiant du paramètre tarifaire est obligatoire")
        @Positive(message = "L'identifiant du paramètre tarifaire doit être positif")
        Long id,

        @NotNull(message = "Le tarif de base est obligatoire")
        @DecimalMin(value = "0.0", message = "Le tarif de base ne peut pas être négatif")
        BigDecimal tarifBase,

        @NotNull(message = "Le prix par kilomètre est obligatoire")
        @DecimalMin(value = "0.01", message = "Le prix par kilomètre doit être strictement supérieur à 0")
        BigDecimal prixKm,

        @NotNull(message = "Le pourcentage de commission est obligatoire")
        @DecimalMin(value = "0.0", message = "Le pourcentage de commission ne peut pas être négatif")
        @DecimalMax(value = "100.0", message = "Le pourcentage de commission ne peut pas dépasser 100%")
        BigDecimal commissionPourcentage,

        @DecimalMin(value = "0.0", message = "Le poids de proximité ne peut pas être négatif")
        @DecimalMax(value = "1000.0", message = "Le poids de proximité ne peut pas dépasser 1000")
        BigDecimal poidsProximite,

        @DecimalMin(value = "0.0", message = "Le poids d'équité ne peut pas être négatif")
        @DecimalMax(value = "1000.0", message = "Le poids d'équité ne peut pas dépasser 1000")
        BigDecimal poidsEquite
) {
    public ParametreTarifRequest toParametreTarifRequest() {
        return new ParametreTarifRequest(tarifBase, prixKm, commissionPourcentage, poidsProximite, poidsEquite);
    }
}
