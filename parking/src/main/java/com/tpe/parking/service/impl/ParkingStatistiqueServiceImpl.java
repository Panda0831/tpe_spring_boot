package com.tpe.parking.service.impl;

import com.tpe.parking.dto.response.ParkingOccupancyResponse;
import com.tpe.parking.dto.response.ParkingRevenueResponse;
import com.tpe.parking.dto.response.ParkingZoneOccupancyResponse;
import com.tpe.parking.entity.EntreeSortie;
import com.tpe.parking.entity.Place;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.repository.EntreeSortieRepository;
import com.tpe.parking.repository.PlaceRepository;
import com.tpe.parking.service.ParkingStatistiqueService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class ParkingStatistiqueServiceImpl implements ParkingStatistiqueService {

    private final EntreeSortieServiceImpl entreeSortieService;
    private final PlaceServiceImpl placeService;
    private final UtilisateurServiceImpl utilisateurService;
    private final VehiculeServiceImpl vehiculeService;
    private final AbonnementServiceImpl abonnementService;
    private final TarifServiceImpl tarifService;
    private final PlaceRepository placeRepository;
    private final EntreeSortieRepository entreeSortieRepository;

    public ParkingStatistiqueServiceImpl(EntreeSortieServiceImpl entreeSortieService,
                                         PlaceServiceImpl placeService,
                                         UtilisateurServiceImpl utilisateurService,
                                         VehiculeServiceImpl vehiculeService,
                                         AbonnementServiceImpl abonnementService,
                                         TarifServiceImpl tarifService,
                                         PlaceRepository placeRepository,
                                         EntreeSortieRepository entreeSortieRepository) {
        this.entreeSortieService = entreeSortieService;
        this.placeService = placeService;
        this.utilisateurService = utilisateurService;
        this.vehiculeService = vehiculeService;
        this.abonnementService = abonnementService;
        this.tarifService = tarifService;
        this.placeRepository = placeRepository;
        this.entreeSortieRepository = entreeSortieRepository;
    }

    @Override
    public long getNombreTotalUtilisateurs() {
        return utilisateurService.getAllUtilisateurs().size();
    }

    @Override
    public long getNombreTotalVehicules() {
        return vehiculeService.getAllVehicules().size();
    }

    @Override
    public long getNombreTotalAbonnements() {
        return abonnementService.getAllAbonnements().size();
    }

    @Override
    public long getNombreTotalPlaces() {
        return placeRepository.count();
    }

    @Override
    public long getNombreTotalEntreesSorties() {
        return entreeSortieRepository.count();
    }

    @Override
    public long getNombreTotalTarifs() {
        return tarifService.getAllTarifs().size();
    }

    @Override
    public long getNombreVehiculesPresents() {
        return entreeSortieRepository.findByDateSortieIsNull().size();
    }

    @Override
    public ParkingOccupancyResponse getOccupation() {
        return construireOccupation(placeRepository.findAll());
    }

    @Override
    public List<ParkingZoneOccupancyResponse> getOccupationParZone() {
        Map<String, List<Place>> parZone = new LinkedHashMap<>();
        for (Place place : placeRepository.findAll()) {
            parZone.computeIfAbsent(place.getZone() == null ? "—" : place.getZone(),
                    k -> new ArrayList<>()).add(place);
        }
        List<ParkingZoneOccupancyResponse> resultat = new ArrayList<>();
        for (Map.Entry<String, List<Place>> entry : parZone.entrySet()) {
            resultat.add(construireOccupationZone(entry.getKey(), entry.getValue()));
        }
        return resultat;
    }

    @Override
    public ParkingRevenueResponse getRevenuDuJour() {
        return calculerRevenu(LocalDate.now());
    }

    @Override
    public List<ParkingRevenueResponse> getRevenusParJour(int nombreDeJours) {
        List<ParkingRevenueResponse> revenus = new ArrayList<>();
        LocalDate aujourdHui = LocalDate.now();
        for (int i = nombreDeJours - 1; i >= 0; i--) {
            revenus.add(calculerRevenu(aujourdHui.minusDays(i)));
        }
        return revenus;
    }

    // ------------------------------------------------------------------
    // Interne
    // ------------------------------------------------------------------

    private ParkingOccupancyResponse construireOccupation(List<Place> places) {
        long total = places.size();
        long libres = compter(places, PlaceStatut.LIBRE);
        long occupees = compter(places, PlaceStatut.OCCUPEE);
        long reservees = compter(places, PlaceStatut.RESERVEE);
        long horsService = compter(places, PlaceStatut.HORS_SERVICE);
        return new ParkingOccupancyResponse(total, libres, occupees, reservees, horsService,
                taux(occupees + reservees, total));
    }

    private ParkingZoneOccupancyResponse construireOccupationZone(String zone, List<Place> places) {
        long total = places.size();
        long libres = compter(places, PlaceStatut.LIBRE);
        long occupees = compter(places, PlaceStatut.OCCUPEE);
        long reservees = compter(places, PlaceStatut.RESERVEE);
        long horsService = compter(places, PlaceStatut.HORS_SERVICE);
        return new ParkingZoneOccupancyResponse(zone, total, libres, occupees, reservees, horsService,
                taux(occupees + reservees, total));
    }

    private long compter(List<Place> places, PlaceStatut statut) {
        return places.stream().filter(p -> p.getStatut() == statut).count();
    }

    private double taux(long occupees, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(occupees * 10000.0 / total) / 100.0;
    }

    private ParkingRevenueResponse calculerRevenu(LocalDate jour) {
        LocalDateTime debut = jour.atStartOfDay();
        LocalDateTime fin = jour.plusDays(1).atStartOfDay();
        List<EntreeSortie> sorties = entreeSortieRepository.findByDateSortieBetween(debut, fin);

        BigDecimal total = sorties.stream()
                .map(EntreeSortie::getMontant)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal moyen = sorties.isEmpty()
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : total.divide(BigDecimal.valueOf(sorties.size()), 2, RoundingMode.HALF_UP);

        return new ParkingRevenueResponse(jour, total, sorties.size(), moyen);
    }
}
