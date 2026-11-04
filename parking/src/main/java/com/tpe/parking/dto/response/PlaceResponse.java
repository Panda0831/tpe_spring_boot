package com.tpe.parking.dto.response;

import com.tpe.parking.entity.Place;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.PlaceType;

public record PlaceResponse(
        Long id,
        String numero,
        String zone,
        PlaceType type,
        PlaceStatut statut
) {
    public static PlaceResponse fromEntity(Place place) {
        if (place == null) return null;

        return new PlaceResponse(
                place.getId(),
                place.getNumero(),
                place.getZone(),
                place.getType(),
                place.getStatut()
        );
    }
}
