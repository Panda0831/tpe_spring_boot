package com.tpe.parking.dto.response;

import java.math.BigDecimal;

import com.tpe.parking.entity.Tarif;

public record TarifResponse(
        Long id,
        String zone,
        BigDecimal montantParHeure,
        BigDecimal tarifMinimum,
        BigDecimal forfaitJournalier,
        BigDecimal reductionAbonne,
        BigDecimal majorationApres
) {
    public static TarifResponse fromEntity(Tarif tarif) {
        if (tarif == null) return null;

        return new TarifResponse(
                tarif.getId(),
                tarif.getZone(),
                tarif.getMontantParHeure(),
                tarif.getTarifMinimum(),
                tarif.getForfaitJournalier(),
                tarif.getReductionAbonne(),
                tarif.getMajorationApres()
        );
    }
}
