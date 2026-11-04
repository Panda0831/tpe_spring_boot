package com.tpe.parking.controller.web;

import com.tpe.parking.dto.request.CreateAbonnementRequest;
import com.tpe.parking.dto.request.RenouvelerAbonnementRequest;
import com.tpe.parking.dto.response.AbonnementResponse;
import com.tpe.parking.entity.AbonnementStatut;
import com.tpe.parking.entity.AbonnementType;
import com.tpe.parking.service.AbonnementService;
import com.tpe.parking.service.UtilisateurService;
import com.tpe.parking.service.VehiculeService;
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
 * Gestion des abonnements : souscription, renouvellement (avec prolongation automatique selon le type)
 * et résiliation. La validité est calculée automatiquement par le domaine ({@code estValide()}).
 */
@Controller
@RequestMapping("/abonnements")
@RequiredArgsConstructor
public class AbonnementWebController {

    private static final int TAILLE_PAGE = 10;

    private final AbonnementService abonnementService;
    private final UtilisateurService utilisateurService;
    private final VehiculeService vehiculeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String liste(@RequestParam(required = false) AbonnementStatut statut,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<AbonnementResponse> resultats = abonnementService.rechercherAbonnements(
                statut, null, null,
                PageRequest.of(Math.max(page, 0), TAILLE_PAGE, Sort.by(Sort.Direction.ASC, "dateFin")));

        model.addAttribute("page", resultats);
        model.addAttribute("statuts", AbonnementStatut.values());
        model.addAttribute("types", AbonnementType.values());
        model.addAttribute("statutSelectionne", statut);
        model.addAttribute("utilisateurs", utilisateurService.getAllUtilisateurs());
        model.addAttribute("vehicules", vehiculeService.getAllVehicules());
        return "abonnements";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String souscrire(@Valid @ModelAttribute CreateAbonnementRequest request,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", FormValidation.message(bindingResult));
            return "redirect:/abonnements";
        }
        try {
            abonnementService.souscrireAbonnement(request);
            redirectAttributes.addFlashAttribute("succes", "Abonnement souscrit avec succès.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/abonnements";
    }

    @PostMapping("/{id}/renouveler")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String renouveler(@PathVariable Long id,
                             @Valid @ModelAttribute RenouvelerAbonnementRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", FormValidation.message(bindingResult));
            return "redirect:/abonnements";
        }
        try {
            abonnementService.renouvelerAbonnement(id, request.nouvelleDateFin());
            redirectAttributes.addFlashAttribute("succes", "Abonnement #" + id + " renouvelé.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/abonnements";
    }

    @PostMapping("/{id}/resilier")
    @PreAuthorize("hasRole('ADMIN')")
    public String resilier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            abonnementService.resilierAbonnement(id);
            redirectAttributes.addFlashAttribute("succes", "Abonnement #" + id + " résilié.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/abonnements";
    }
}
