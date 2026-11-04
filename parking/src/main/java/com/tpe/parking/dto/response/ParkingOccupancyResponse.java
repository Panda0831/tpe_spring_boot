package com.tpe.parking.dto.response;

public record ParkingOccupancyResponse(
        long totalPlaces,
        long placesLibres,
        long placesOccupees,
        long placesReservees,
        long placesHorsService,
        double tauxOccupationPourcentage
) {}
