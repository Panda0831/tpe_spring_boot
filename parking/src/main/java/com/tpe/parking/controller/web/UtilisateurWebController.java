package com.tpe.parking.controller.web;

import com.tpe.parking.dto.request.CreateUtilisateurRequest;
import com.tpe.parking.dto.response.UtilisateurResponse;
import com.tpe.parking.entity.Role;
import com.tpe.parking.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

/**
 * Gestion des utilisateurs (comptes clients & personnel) : recherche paginée,
 * création de compte avec rôle (MANAGER+), suppression (ADMIN).
 */
@Controller
@RequestMapping("/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurWebController {

    private static final int TAILLE_PAGE = 10;

    private final UtilisateurService utilisateurService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String liste(@RequestParam(required = false) String recherche,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), TAILLE_PAGE, Sort.by("nom", "prenom"));
        Page<UtilisateurResponse> resultats = utilisateurService.rechercherUtilisateurs(
                recherche != null ? recherche.trim() : null, pageable);

        model.addAttribute("page", resultats);
        model.addAttribute("recherche", recherche);
        model.addAttribute("roles", Role.values());
        return "utilisateurs";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String creer(@Valid @ModelAttribute CreateUtilisateurRequest request,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", FormValidation.message(bindingResult));
            return "redirect:/utilisateurs";
        }
        try {
            UtilisateurResponse utilisateur = utilisateurService.creerUtilisateur(new CreateUtilisateurRequest(
                    vide(request.nom()), vide(request.prenom()), vide(request.telephone()), vide(request.email()),
                    vide(request.motDePasse()) != null ? request.motDePasse() : "password",
                    request.role() != null ? request.role() : Role.USER));
            redirectAttributes.addFlashAttribute("succes",
                    "Utilisateur " + utilisateur.prenom() + " " + utilisateur.nom() + " (" + utilisateur.email() + ") créé.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/utilisateurs";
    }

    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.supprimerUtilisateur(id);
            redirectAttributes.addFlashAttribute("succes", "Utilisateur #" + id + " supprimé.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/utilisateurs";
    }

    private String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
