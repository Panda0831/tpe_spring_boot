package com.tpe.taxis.controller.rest;

import com.tpe.taxis.dto.response.ActivityStatsResponse;
import com.tpe.taxis.dto.response.RevenueStatsResponse;
import com.tpe.taxis.service.TaxiStatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class TaxiStatsRestController {

    private final TaxiStatistiqueService taxiStatistiqueService;

    /**
     * Chiffre d'affaires sur une période. Sans paramètre, les 30 derniers jours sont analysés.
     * Exemple : {@code GET /api/stats/revenue?dateDebut=2026-09-01T00:00:00&dateFin=2026-09-30T23:59:59}.
     */
    @GetMapping("/revenue")
    public RevenueStatsResponse getRevenueStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        return taxiStatistiqueService.getRevenueStats(debutParDefaut(dateDebut), finParDefaut(dateFin));
    }

    /**
     * Statistiques d'activité sur une période. Sans paramètre, les 30 derniers jours sont analysés.
     */
    @GetMapping("/activity")
    public ActivityStatsResponse getActivityStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        return taxiStatistiqueService.getActivityStats(debutParDefaut(dateDebut), finParDefaut(dateFin));
    }

    private LocalDateTime debutParDefaut(LocalDateTime dateDebut) {
        return dateDebut != null ? dateDebut : LocalDate.now().minusDays(30).atStartOfDay();
    }

    private LocalDateTime finParDefaut(LocalDateTime dateFin) {
        return dateFin != null ? dateFin : LocalDate.now().plusDays(1).atStartOfDay();
    }
}
