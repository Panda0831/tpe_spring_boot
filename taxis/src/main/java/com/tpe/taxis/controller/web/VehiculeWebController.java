package com.tpe.taxis.controller.web;

import com.tpe.taxis.dto.request.CreateVehiculeRequest;
import com.tpe.taxis.dto.request.AffecterChauffeurRequest;
import com.tpe.taxis.dto.request.ChangerVehiculeStatutRequest;
import com.tpe.taxis.dto.request.UpdateVehiculeRequest;
import com.tpe.taxis.dto.response.VehiculeResponse;
import com.tpe.taxis.entity.VehiculeStatut;
import com.tpe.taxis.service.ChauffeurService;
import com.tpe.taxis.service.VehiculeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Parc de véhicules : recherche paginée, filtre par statut, création/modification,
 * changement de statut (maintenance, hors service) et suppression (ADMIN).
 */
@Controller
@RequestMapping("/vehicules")
@RequiredArgsConstructor
public class VehiculeWebController {

    private static final int TAILLE_PAGE = 10;

    private final VehiculeService vehiculeService;
    private final ChauffeurService chauffeurService;

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche,
                        @RequestParam(required = false) VehiculeStatut statut,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), TAILLE_PAGE, Sort.by("immatriculation"));
        Page<VehiculeResponse> resultats;

        if (recherche != null && !recherche.isBlank()) {
            resultats = vehiculeService.searchVehicules(recherche.trim(), recherche.trim(), pageable);
        } else if (statut != null) {
            resultats = vehiculeService.getVehiculesByStatut(statut, pageable);
        } else {
            resultats = vehiculeService.getAllVehicules(pageable);
        }

        model.addAttribute("page", resultats);
        model.addAttribute("recherche", recherche);
        model.addAttribute("statuts", VehiculeStatut.values());
        model.addAttribute("statutSelectionne", statut);
        model.addAttribute("chauffeurs", chauffeurService.getAllChauffeurs());
        model.addAttribute("vehiculesDisponibles", vehiculeService.getVehiculesDisponiblesPourCourse());
        return "vehicules";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String creer(@Valid @ModelAttribute CreateVehiculeRequest request,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/vehicules";
        }
        try {
            VehiculeResponse vehicule = vehiculeService.createVehicule(request);
            redirectAttributes.addFlashAttribute("succes",
                    "Véhicule " + vehicule.immatriculation() + " (" + vehicule.marque() + " " + vehicule.modele()
                            + ") enregistré.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/vehicules";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String modifier(@PathVariable Long id,
                           @Valid @ModelAttribute UpdateVehiculeRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/vehicules";
        }
        try {
            vehiculeService.updateVehicule(id, request);
            redirectAttributes.addFlashAttribute("succes", "Véhicule #" + id + " mis à jour.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/vehicules";
    }

    @PostMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String changerStatut(@PathVariable Long id,
                                @Valid @ModelAttribute ChangerVehiculeStatutRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/vehicules";
        }
        try {
            vehiculeService.changeStatut(id, request.statut());
            redirectAttributes.addFlashAttribute("succes",
                    "Véhicule #" + id + " passé au statut " + request.statut() + ".");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/vehicules";
    }

    @PostMapping("/{id}/chauffeur")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String affecterChauffeur(@PathVariable Long id,
                                    @Valid @ModelAttribute AffecterChauffeurRequest request,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/vehicules";
        }
        try {
            if (request.chauffeurId() == null) {
                vehiculeService.retirerChauffeur(id);
                redirectAttributes.addFlashAttribute("succes", "Chauffeur retiré du véhicule #" + id + ".");
            } else {
                vehiculeService.affecterChauffeur(id, request.chauffeurId());
                redirectAttributes.addFlashAttribute("succes", "Chauffeur affecté au véhicule #" + id + ".");
            }
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/vehicules";
    }

    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vehiculeService.deleteVehicule(id);
            redirectAttributes.addFlashAttribute("succes", "Véhicule #" + id + " supprimé.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/vehicules";
    }

    private String messageValidation(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Les informations du véhicule sont invalides.");
    }
}
