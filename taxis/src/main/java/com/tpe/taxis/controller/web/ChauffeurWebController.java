package com.tpe.taxis.controller.web;

import com.tpe.taxis.dto.request.CreateChauffeurRequest;
import com.tpe.taxis.dto.request.AffecterVehiculeRequest;
import com.tpe.taxis.dto.request.ChangerChauffeurStatutRequest;
import com.tpe.taxis.dto.request.UpdateChauffeurRequest;
import com.tpe.taxis.dto.response.ChauffeurResponse;
import com.tpe.taxis.entity.ChauffeurStatut;
import com.tpe.taxis.service.ChauffeurService;
import com.tpe.taxis.service.VehiculeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Fiches chauffeurs : recherche paginée, alerte visuelle sur les permis expirés ou proches
 * de l'expiration, création/modification et changement de statut (MANAGER+).
 */
@Controller
@RequestMapping("/chauffeurs")
@RequiredArgsConstructor
public class ChauffeurWebController {

    private static final int TAILLE_PAGE = 10;
    private static final int JOURS_ALERTE_PERMIS = 30;

    private final ChauffeurService chauffeurService;
    private final VehiculeService vehiculeService;

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche,
                        @RequestParam(required = false) ChauffeurStatut statut,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), TAILLE_PAGE, Sort.by("nom", "prenom"));
        Page<ChauffeurResponse> resultats;

        if (recherche != null && !recherche.isBlank()) {
            resultats = chauffeurService.searchChauffeurs(recherche.trim(), recherche.trim(), pageable);
        } else if (statut != null) {
            resultats = chauffeurService.getChauffeursByStatut(statut, pageable);
        } else {
            resultats = chauffeurService.getAllChauffeurs(pageable);
        }

        model.addAttribute("page", resultats);
        model.addAttribute("recherche", recherche);
        model.addAttribute("statuts", ChauffeurStatut.values());
        model.addAttribute("statutSelectionne", statut);
        model.addAttribute("vehicules", vehiculeService.getAllVehicules());
        model.addAttribute("dateAlertePermis", LocalDate.now().plusDays(JOURS_ALERTE_PERMIS));
        model.addAttribute("chauffeursEligibles", chauffeurService.getChauffeursEligiblesPourCourse());
        return "chauffeurs";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String creer(@Valid @ModelAttribute CreateChauffeurRequest request,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/chauffeurs";
        }
        try {
            ChauffeurResponse chauffeur = chauffeurService.createChauffeur(request);
            redirectAttributes.addFlashAttribute("succes",
                    "Chauffeur " + chauffeur.nomComplet() + " (" + chauffeur.numeroPermis() + ") enregistré.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/chauffeurs";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String modifier(@PathVariable Long id,
                           @Valid @ModelAttribute UpdateChauffeurRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/chauffeurs";
        }
        try {
            chauffeurService.updateChauffeur(id, request);
            redirectAttributes.addFlashAttribute("succes", "Chauffeur #" + id + " mis à jour.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/chauffeurs";
    }

    @PostMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String changerStatut(@PathVariable Long id,
                                @Valid @ModelAttribute ChangerChauffeurStatutRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/chauffeurs";
        }
        try {
            chauffeurService.changeStatut(id, request.statut());
            redirectAttributes.addFlashAttribute("succes",
                    "Chauffeur #" + id + " passé au statut " + request.statut() + ".");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/chauffeurs";
    }

    @PostMapping("/{id}/vehicule")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String affecterVehicule(@PathVariable Long id,
                                   @Valid @ModelAttribute AffecterVehiculeRequest request,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/chauffeurs";
        }
        try {
            if (request.vehiculeId() == null) {
                chauffeurService.retirerVehicule(id);
                redirectAttributes.addFlashAttribute("succes", "Véhicule retiré du chauffeur #" + id + ".");
            } else {
                chauffeurService.affecterVehicule(id, request.vehiculeId());
                redirectAttributes.addFlashAttribute("succes", "Véhicule affecté au chauffeur #" + id + ".");
            }
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/chauffeurs";
    }

    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            chauffeurService.deleteChauffeur(id);
            redirectAttributes.addFlashAttribute("succes", "Chauffeur #" + id + " supprimé.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/chauffeurs";
    }

    private String messageValidation(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Les informations du chauffeur sont invalides.");
    }
}
