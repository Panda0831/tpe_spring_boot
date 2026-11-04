package com.tpe.taxis.dto.response;

import com.tpe.taxis.entity.ParametreTarif;

import java.math.BigDecimal;

public record ParametreTarifResponse(
        Long id,
        BigDecimal tarifBase,
        BigDecimal prixKm,
        BigDecimal commissionPourcentage,
        BigDecimal poidsProximite,
        BigDecimal poidsEquite
) {
    public static ParametreTarifResponse fromEntity(ParametreTarif entity) {
        if (entity == null) {
            return null;
        }

        return new ParametreTarifResponse(
                entity.getId(),
                entity.getTarifBase(),
                entity.getPrixKm(),
                entity.getCommissionPourcentage(),
                entity.getPoidsProximite() != null
                        ? entity.getPoidsProximite() : ParametreTarif.DEFAULT_POIDS_PROXIMITE,
                entity.getPoidsEquite() != null
                        ? entity.getPoidsEquite() : ParametreTarif.DEFAULT_POIDS_EQUITE
        );
    }
}
