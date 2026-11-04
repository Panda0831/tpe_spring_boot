package com.tpe.parking.controller.web;

import com.tpe.parking.service.ParkingStatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Tableau de bord : indicateurs clés (occupation, chiffre d'affaires du jour, volumes).
 */
@Controller
@RequiredArgsConstructor
public class DashboardWebController {

    private final ParkingStatistiqueService statistiqueService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"))) {
            return "redirect:/places";
        }
        model.addAttribute("occupation", statistiqueService.getOccupation());
        model.addAttribute("occupationZones", statistiqueService.getOccupationParZone());
        model.addAttribute("revenuJour", statistiqueService.getRevenuDuJour());
        model.addAttribute("nbUtilisateurs", statistiqueService.getNombreTotalUtilisateurs());
        model.addAttribute("nbVehicules", statistiqueService.getNombreTotalVehicules());
        model.addAttribute("nbAbonnements", statistiqueService.getNombreTotalAbonnements());
        model.addAttribute("nbPlaces", statistiqueService.getNombreTotalPlaces());
        model.addAttribute("nbEntreesSorties", statistiqueService.getNombreTotalEntreesSorties());
        model.addAttribute("nbPresents", statistiqueService.getNombreVehiculesPresents());
        return "dashboard";
    }
}
