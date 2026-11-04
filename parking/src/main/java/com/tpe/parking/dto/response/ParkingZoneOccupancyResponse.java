package com.tpe.parking.dto.response;

/**
 * Taux d'occupation d'une zone du parking.
 */
public record ParkingZoneOccupancyResponse(
        String zone,
        long totalPlaces,
        long placesLibres,
        long placesOccupees,
        long placesReservees,
        long placesHorsService,
        double tauxOccupationPourcentage
) {}
