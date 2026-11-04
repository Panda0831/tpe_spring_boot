package com.tpe.taxis.controller.web;

import com.tpe.taxis.dto.response.ActivityStatsResponse;
import com.tpe.taxis.dto.response.CourseResponse;
import com.tpe.taxis.dto.response.RevenueStatsResponse;
import com.tpe.taxis.entity.CourseStatut;
import com.tpe.taxis.service.ChauffeurService;
import com.tpe.taxis.service.CourseService;
import com.tpe.taxis.service.TaxiStatistiqueService;
import com.tpe.taxis.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tableau de bord : indicateurs du jour (courses en cours, CA, taux d'annulation),
 * disponibilité de la flotte et courses à traiter.
 */
@Controller
@RequiredArgsConstructor
public class DashboardWebController {

    private final TaxiStatistiqueService statistiqueService;
    private final CourseService courseService;
    private final ChauffeurService chauffeurService;
    private final VehiculeService vehiculeService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        LocalDate aujourdHui = LocalDate.now();
        LocalDateTime debutJour = aujourdHui.atStartOfDay();
        LocalDateTime finJour = aujourdHui.plusDays(1).atStartOfDay();

        ActivityStatsResponse activiteJour = statistiqueService.getActivityStats(debutJour, finJour);
        RevenueStatsResponse revenusJour = statistiqueService.getRevenueStats(debutJour, finJour);
        ActivityStatsResponse activite30Jours = statistiqueService.getActivityStats(
                aujourdHui.minusDays(30).atStartOfDay(), LocalDateTime.now());

        model.addAttribute("activiteJour", activiteJour);
        model.addAttribute("revenusJour", revenusJour);
        model.addAttribute("activite30Jours", activite30Jours);
        model.addAttribute("chauffeurs", chauffeurService.getAllChauffeurs());
        model.addAttribute("chauffeursDisponibles", chauffeurService.getChauffeursEligiblesPourCourse());
        model.addAttribute("vehicules", vehiculeService.getAllVehicules());
        model.addAttribute("vehiculesDisponibles", vehiculeService.getVehiculesDisponiblesPourCourse());
        model.addAttribute("coursesATraiter", coursesATraiter());
        return "dashboard";
    }

    /** Courses non encore clôturées (EN_ATTENTE, ASSIGNEE, EN_COURS), les plus proches d'abord. */
    private List<CourseResponse> coursesATraiter() {
        List<CourseResponse> actives = new ArrayList<>();
        for (CourseStatut statut : List.of(CourseStatut.EN_ATTENTE, CourseStatut.ASSIGNEE, CourseStatut.EN_COURS)) {
            actives.addAll(courseService.getCoursesByStatut(statut,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "dateHeure"))).getContent());
        }
        actives.sort(java.util.Comparator.comparing(CourseResponse::dateHeure,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        return actives;
    }
}
