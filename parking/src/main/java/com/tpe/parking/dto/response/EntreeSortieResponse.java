package com.tpe.parking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tpe.parking.entity.EntreeSortie;

public record EntreeSortieResponse(
        Long id,
        Long vehiculeId,
        String immatriculationVehicule,
        Long placeId,
        String numeroPlace,
        LocalDateTime dateEntree,
        LocalDateTime dateSortie,
        LocalDateTime dateDepartPrevue,
        BigDecimal montant,
        boolean enCours
) {
    public static EntreeSortieResponse fromEntity(EntreeSortie es) {
        if (es == null) return null;

        return new EntreeSortieResponse(
                es.getId(),
                es.getVehicule() != null ? es.getVehicule().getId() : null,
                es.getVehicule() != null ? es.getVehicule().getImmatriculation() : null,
                es.getPlace() != null ? es.getPlace().getId() : null,
                es.getPlace() != null ? es.getPlace().getNumero() : null,
                es.getDateEntree(),
                es.getDateSortie(),
                es.getDateDepartPrevue(),
                es.getMontant(),
                es.getDateSortie() == null
        );
    }
}
