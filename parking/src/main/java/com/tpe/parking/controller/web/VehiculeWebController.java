package com.tpe.parking.controller.web;

import com.tpe.parking.dto.request.CreateVehiculeRequest;
import com.tpe.parking.dto.response.VehiculeResponse;
import com.tpe.parking.service.UtilisateurService;
import com.tpe.parking.service.VehiculeService;
import com.tpe.parking.security.CustomUserDetailsService.UtilisateurPrincipal;
import com.tpe.parking.security.UtilisateurAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

/**
 * Gestion web des véhicules du parking : listing paginé, recherche par plaque/marque/modèle,
 * enregistrement d'un véhicule associé à un propriétaire (MANAGER+).
 */
@Controller
@RequestMapping("/vehicules")
@RequiredArgsConstructor
public class VehiculeWebController {

    private static final int TAILLE_PAGE = 10;

    private final VehiculeService vehiculeService;
    private final UtilisateurService utilisateurService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','OPERATOR','MANAGER','ADMIN')")
    public String liste(@RequestParam(required = false) String recherche,
                        @RequestParam(required = false) Long utilisateurId,
                        @RequestParam(defaultValue = "0") int page,
                        @AuthenticationPrincipal UtilisateurPrincipal principal,
                        Model model) {
        utilisateurId = UtilisateurAccess.verifierFiltreUtilisateur(principal, utilisateurId);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), TAILLE_PAGE, Sort.by("immatriculation"));
        Page<VehiculeResponse> resultats = vehiculeService.rechercherVehicules(
                recherche != null ? recherche.trim() : null, utilisateurId, pageable);

        model.addAttribute("page", resultats);
        model.addAttribute("recherche", recherche);
        model.addAttribute("utilisateurId", utilisateurId);
        model.addAttribute("utilisateurs", UtilisateurAccess.estUtilisateur(principal)
                ? java.util.List.of() : utilisateurService.getAllUtilisateurs());
        return "vehicules";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String creer(@Valid @ModelAttribute CreateVehiculeRequest request,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", FormValidation.message(bindingResult));
            return "redirect:/vehicules";
        }
        try {
            VehiculeResponse vehicule = vehiculeService.createVehicule(new CreateVehiculeRequest(
                    vide(request.immatriculation()), vide(request.marque()), vide(request.modele()), request.utilisateurId()));
            redirectAttributes.addFlashAttribute("succes",
                    "Véhicule " + vehicule.immatriculation() + " (" + vehicule.marque() + " " + vehicule.modele() + ") enregistré.");
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

    private String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
