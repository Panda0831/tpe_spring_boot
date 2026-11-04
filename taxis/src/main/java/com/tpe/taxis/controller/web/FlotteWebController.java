package com.tpe.taxis.controller.web;

import com.tpe.taxis.dto.response.ChauffeurResponse;
import com.tpe.taxis.dto.response.VehiculeResponse;
import com.tpe.taxis.entity.ChauffeurStatut;
import com.tpe.taxis.entity.VehiculeStatut;
import com.tpe.taxis.service.ChauffeurService;
import com.tpe.taxis.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vue synoptique de la disponibilité : véhicules et chauffeurs regroupés par statut
 * (pastilles de couleur), avec les chauffeurs éligibles pour une course et les permis
 * expirés ou proches de l'expiration.
 */
@Controller
@RequiredArgsConstructor
public class FlotteWebController {

    private static final int JOURS_ALERTE_PERMIS = 30;

    private final VehiculeService vehiculeService;
    private final ChauffeurService chauffeurService;

    @GetMapping("/flotte")
    public String flotte(Model model) {
        Map<String, List<VehiculeResponse>> vehiculesParStatut = new LinkedHashMap<>();
        for (VehiculeStatut statut : VehiculeStatut.values()) {
            vehiculesParStatut.put(statut.name(), vehiculeService.getVehiculesByStatut(statut));
        }

        Map<String, List<ChauffeurResponse>> chauffeursParStatut = new LinkedHashMap<>();
        for (ChauffeurStatut statut : ChauffeurStatut.values()) {
            chauffeursParStatut.put(statut.name(), chauffeurService.getChauffeursByStatut(statut));
        }

        model.addAttribute("vehiculesParStatut", vehiculesParStatut);
        model.addAttribute("chauffeursParStatut", chauffeursParStatut);
        model.addAttribute("vehicules", vehiculeService.getAllVehicules());
        model.addAttribute("chauffeurs", chauffeurService.getAllChauffeurs());
        model.addAttribute("vehiculesDisponibles", vehiculeService.getVehiculesDisponiblesPourCourse());
        model.addAttribute("chauffeursEligibles", chauffeurService.getChauffeursEligiblesPourCourse());
        model.addAttribute("dateAlertePermis", LocalDate.now().plusDays(JOURS_ALERTE_PERMIS));
        return "flotte";
    }
}
