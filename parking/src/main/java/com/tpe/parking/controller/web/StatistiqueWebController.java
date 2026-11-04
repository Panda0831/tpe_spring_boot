package com.tpe.parking.controller.web;

import com.tpe.parking.dto.response.ParkingRevenueResponse;
import com.tpe.parking.service.ParkingStatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Statistiques d'occupation et de facturation : taux global, détail par zone et
 * chiffre d'affaires journalier (histogramme) sur une période glissante.
 */
@Controller
@RequiredArgsConstructor
public class StatistiqueWebController {

    private final ParkingStatistiqueService statistiqueService;

    /** Ligne d'histogramme : le pourcentage de hauteur est calculé côté serveur (pas de calcul SpEL). */
    public record RevenuJour(LocalDate date, BigDecimal montant, long nombreSorties, int pourcentage) {
    }

    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String statistiques(@RequestParam(defaultValue = "7") int jours, Model model) {
        int periode = Math.min(Math.max(jours, 1), 60);
        List<ParkingRevenueResponse> revenus = statistiqueService.getRevenusParJour(periode);

        BigDecimal maximum = revenus.stream()
                .map(ParkingRevenueResponse::chiffreAffairesTotal)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalPeriode = revenus.stream()
                .map(ParkingRevenueResponse::chiffreAffairesTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RevenuJour> lignes = revenus.stream()
                .map(r -> new RevenuJour(
                        r.dateRapport(),
                        r.chiffreAffairesTotal(),
                        r.nombreSortiesFacturees(),
                        pourcentage(r.chiffreAffairesTotal(), maximum)))
                .toList();

        model.addAttribute("occupation", statistiqueService.getOccupation());
        model.addAttribute("zones", statistiqueService.getOccupationParZone());
        model.addAttribute("revenuJour", statistiqueService.getRevenuDuJour());
        model.addAttribute("revenus", lignes);
        model.addAttribute("totalPeriode", totalPeriode);
        model.addAttribute("periode", periode);
        model.addAttribute("nbPlaces", statistiqueService.getNombreTotalPlaces());
        model.addAttribute("nbUtilisateurs", statistiqueService.getNombreTotalUtilisateurs());
        model.addAttribute("nbVehicules", statistiqueService.getNombreTotalVehicules());
        model.addAttribute("nbAbonnements", statistiqueService.getNombreTotalAbonnements());
        model.addAttribute("nbEntreesSorties", statistiqueService.getNombreTotalEntreesSorties());
        model.addAttribute("nbPresents", statistiqueService.getNombreVehiculesPresents());
        return "statistiques";
    }

    private int pourcentage(BigDecimal valeur, BigDecimal maximum) {
        if (maximum == null || maximum.signum() <= 0 || valeur == null) {
            return 0;
        }
        return valeur.multiply(BigDecimal.valueOf(100))
                .divide(maximum, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }
}
