package com.tpe.taxis.mapper;

import com.tpe.taxis.dto.request.ParametreTarifRequest;
import com.tpe.taxis.dto.response.ParametreTarifResponse;
import com.tpe.taxis.entity.ParametreTarif;

public final class ParametreTarifMapper {

    private ParametreTarifMapper() {
    }

    public static ParametreTarifResponse toResponse(ParametreTarif entity) {
        if (entity == null) {
            return null;
        }
        return ParametreTarifResponse.fromEntity(entity);
    }

    public static ParametreTarif toEntity(ParametreTarifRequest request) {
        if (request == null) {
            return null;
        }
        return ParametreTarif.builder()
                .tarifBase(request.tarifBase())
                .prixKm(request.prixKm())
                .commissionPourcentage(request.commissionPourcentage())
                .poidsProximite(request.poidsProximite() != null
                        ? request.poidsProximite() : ParametreTarif.DEFAULT_POIDS_PROXIMITE)
                .poidsEquite(request.poidsEquite() != null
                        ? request.poidsEquite() : ParametreTarif.DEFAULT_POIDS_EQUITE)
                .build();
    }

    public static void updateEntity(ParametreTarif entity, ParametreTarifRequest request) {
        if (entity == null || request == null) {
            return;
        }
        if (request.tarifBase() != null) {
            entity.setTarifBase(request.tarifBase());
        }
        if (request.prixKm() != null) {
            entity.setPrixKm(request.prixKm());
        }
        if (request.commissionPourcentage() != null) {
            entity.setCommissionPourcentage(request.commissionPourcentage());
        }
        if (request.poidsProximite() != null) {
            entity.setPoidsProximite(request.poidsProximite());
        }
        if (request.poidsEquite() != null) {
            entity.setPoidsEquite(request.poidsEquite());
        }
    }
}
