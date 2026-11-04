package com.tpe.parking.service;

import com.tpe.parking.dto.response.ParkingOccupancyResponse;
import com.tpe.parking.dto.response.ParkingRevenueResponse;
import com.tpe.parking.dto.response.ParkingZoneOccupancyResponse;

import java.util.List;

public interface ParkingStatistiqueService {

    long getNombreTotalUtilisateurs();

    long getNombreTotalVehicules();

    long getNombreTotalAbonnements();

    long getNombreTotalPlaces();

    long getNombreTotalEntreesSorties();

    long getNombreTotalTarifs();

    /** Véhicules actuellement présents dans le parking (entrée non clôturée). */
    long getNombreVehiculesPresents();

    /** Occupation globale du parking (par statut de place). */
    ParkingOccupancyResponse getOccupation();

    /** Occupation détaillée zone par zone. */
    List<ParkingZoneOccupancyResponse> getOccupationParZone();

    /** Chiffre d'affaires du jour (facturation des sorties). */
    ParkingRevenueResponse getRevenuDuJour();

    /** Chiffre d'affaires des {@code nombreDeJours} derniers jours, du plus ancien au plus récent. */
    List<ParkingRevenueResponse> getRevenusParJour(int nombreDeJours);
}
