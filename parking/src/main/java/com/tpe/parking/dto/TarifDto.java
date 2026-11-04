package com.tpe.parking.dto;

import com.tpe.parking.entity.Tarif;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TarifDto(
        Long id,
        String zone,
        BigDecimal montantParHeure,
        BigDecimal tarifMinimum,
        BigDecimal forfaitJournalier,
        BigDecimal reductionAbonne,
        BigDecimal majorationApres
) {
    public static TarifDto versDto(Tarif tarif) {
        if (tarif == null) return null;


        return new TarifDto(
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
