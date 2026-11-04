package com.tpe.parking.mapper;

import com.tpe.parking.dto.request.CreateTarifRequest;
import com.tpe.parking.dto.request.UpdateTarifRequest;
import com.tpe.parking.dto.response.TarifResponse;
import com.tpe.parking.entity.Tarif;

public final class TarifMapper {

    private TarifMapper() {
    }

    public static TarifResponse toResponse(Tarif tarif) {
        if (tarif == null) {
            return null;
        }
        return TarifResponse.fromEntity(tarif);
    }

    public static Tarif toEntity(CreateTarifRequest request) {
        if (request == null) {
            return null;
        }
        Tarif tarif = new Tarif();
        tarif.setZone(request.zone());
        tarif.setMontantParHeure(request.montantParHeure());
        tarif.setTarifMinimum(request.tarifMinimum());
        tarif.setForfaitJournalier(request.forfaitJournalier());
        tarif.setReductionAbonne(request.reductionAbonne());
        tarif.setMajorationApres(request.majorationApres());
        return tarif;
    }

    public static void updateEntity(Tarif tarif, UpdateTarifRequest request) {
        if (tarif == null || request == null) {
            return;
        }
        if (request.zone() != null) tarif.setZone(request.zone());
        if (request.montantParHeure() != null) tarif.setMontantParHeure(request.montantParHeure());
        if (request.tarifMinimum() != null) tarif.setTarifMinimum(request.tarifMinimum());
        if (request.forfaitJournalier() != null) tarif.setForfaitJournalier(request.forfaitJournalier());
        if (request.reductionAbonne() != null) tarif.setReductionAbonne(request.reductionAbonne());
        if (request.majorationApres() != null) tarif.setMajorationApres(request.majorationApres());
    }
}
