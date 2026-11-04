package com.tpe.taxis.controller.web;

import com.tpe.taxis.dto.response.ActivityStatsResponse;
import com.tpe.taxis.dto.response.RevenueStatsResponse;
import com.tpe.taxis.service.TaxiStatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rapports financiers et statistiques : chiffre d'affaires, commissions et part chauffeur
 * sur une période, classements par chauffeur et par véhicule, activité (taux d'annulation,
 * distance parcourue, revenu moyen) et évolution journalière du CA.
 */
@Controller
@RequiredArgsConstructor
public class StatistiquesWebController {

    /** Nombre de jours représentés dans l'histogramme (les périodes plus longues sont résumées). */
    private static final int MAX_JOURS_HISTOGRAMME = 14;

    private final TaxiStatistiqueService statistiqueService;

    /** Ligne d'histogramme : le pourcentage est calculé côté serveur. */
    public record RevenuJour(LocalDate jour, BigDecimal chiffreAffaires, BigDecimal commissions,
                             long nombreCourses, int pourcentage) {
    }

    @GetMapping("/statistiques")
    public String statistiques(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            Model model) {
        LocalDate fin = dateFin != null ? dateFin : LocalDate.now();
        LocalDate debut = dateDebut != null ? dateDebut : fin.minusDays(29);
        if (debut.isAfter(fin)) {
            LocalDate echange = debut;
            debut = fin;
            fin = echange;
        }

        LocalDateTime borneDebut = debut.atStartOfDay();
        LocalDateTime borneFin = fin.plusDays(1).atStartOfDay();

        RevenueStatsResponse revenus = statistiqueService.getRevenueStats(borneDebut, borneFin);
        ActivityStatsResponse activite = statistiqueService.getActivityStats(borneDebut, borneFin);

        model.addAttribute("revenus", revenus);
        model.addAttribute("activite", activite);
        model.addAttribute("dateDebut", debut);
        model.addAttribute("dateFin", fin);
        model.addAttribute("revenusParJour", revenusParJour(debut, fin));
        return "statistiques";
    }

    /** Série journalière du CA (limitée à {@value #MAX_JOURS_HISTOGRAMME} jours pour rester lisible). */
    private List<RevenuJour> revenusParJour(LocalDate debut, LocalDate fin) {
        LocalDate premierJour = fin.minusDays(MAX_JOURS_HISTOGRAMME - 1L);
        if (premierJour.isBefore(debut)) {
            premierJour = debut;
        }

        List<RevenueStatsResponse> parJour = new ArrayList<>();
        List<LocalDate> jours = new ArrayList<>();
        for (LocalDate jour = premierJour; !jour.isAfter(fin); jour = jour.plusDays(1)) {
            jours.add(jour);
            parJour.add(statistiqueService.getRevenueStats(jour.atStartOfDay(), jour.plusDays(1).atStartOfDay()));
        }

        BigDecimal maximum = parJour.stream()
                .map(RevenueStatsResponse::chiffreAffairesTotal)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        List<RevenuJour> lignes = new ArrayList<>();
        for (int i = 0; i < jours.size(); i++) {
            RevenueStatsResponse jour = parJour.get(i);
            lignes.add(new RevenuJour(jours.get(i), jour.chiffreAffairesTotal(), jour.commissionTotale(),
                    jour.nombreCoursesTerminees(), pourcentage(jour.chiffreAffairesTotal(), maximum)));
        }
        return lignes;
    }

    private int pourcentage(BigDecimal valeur, BigDecimal maximum) {
        if (maximum == null || maximum.signum() <= 0 || valeur == null) {
            return 0;
        }
        return valeur.multiply(BigDecimal.valueOf(100))
                .divide(maximum, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
