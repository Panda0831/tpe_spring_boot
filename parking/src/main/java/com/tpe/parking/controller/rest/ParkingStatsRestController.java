package com.tpe.parking.controller.rest;

import com.tpe.parking.dto.response.ParkingOccupancyResponse;
import com.tpe.parking.dto.response.ParkingRevenueResponse;
import com.tpe.parking.dto.response.ParkingZoneOccupancyResponse;
import com.tpe.parking.service.ParkingStatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST des statistiques du parking : volumes, taux d'occupation (global et par zone)
 * et chiffre d'affaires (jour / historique).
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class ParkingStatsRestController {

    private final ParkingStatistiqueService parkingStatistiqueService;

    @GetMapping("/utilisateurs/total")
    public long getNombreTotalUtilisateurs() {
        return parkingStatistiqueService.getNombreTotalUtilisateurs();
    }

    @GetMapping("/vehicules/total")
    public long getNombreTotalVehicules() {
        return parkingStatistiqueService.getNombreTotalVehicules();
    }

    @GetMapping("/abonnements/total")
    public long getNombreTotalAbonnements() {
        return parkingStatistiqueService.getNombreTotalAbonnements();
    }

    @GetMapping("/places/total")
    public long getNombreTotalPlaces() {
        return parkingStatistiqueService.getNombreTotalPlaces();
    }

    @GetMapping("/entrees-sorties/total")
    public long getNombreTotalEntreesSorties() {
        return parkingStatistiqueService.getNombreTotalEntreesSorties();
    }

    @GetMapping("/tarifs/total")
    public long getNombreTotalTarifs() {
        return parkingStatistiqueService.getNombreTotalTarifs();
    }

    /** Véhicules actuellement stationnés (entrée sans sortie enregistrée). */
    @GetMapping("/vehicules-presents")
    public long getNombreVehiculesPresents() {
        return parkingStatistiqueService.getNombreVehiculesPresents();
    }

    /** Taux d'occupation global par statut de place. */
    @GetMapping("/occupation")
    public ParkingOccupancyResponse getOccupation() {
        return parkingStatistiqueService.getOccupation();
    }

    /** Taux d'occupation détaillé zone par zone. */
    @GetMapping("/occupation/zones")
    public List<ParkingZoneOccupancyResponse> getOccupationParZone() {
        return parkingStatistiqueService.getOccupationParZone();
    }

    /** Chiffre d'affaires du jour. */
    @GetMapping("/revenus/jour")
    public ParkingRevenueResponse getRevenuDuJour() {
        return parkingStatistiqueService.getRevenuDuJour();
    }

    /** Chiffre d'affaires des {@code jours} derniers jours (7 par défaut), du plus ancien au plus récent. */
    @GetMapping("/revenus")
    public List<ParkingRevenueResponse> getRevenusParJour(@RequestParam(defaultValue = "7") int jours) {
        return parkingStatistiqueService.getRevenusParJour(jours);
    }
}
