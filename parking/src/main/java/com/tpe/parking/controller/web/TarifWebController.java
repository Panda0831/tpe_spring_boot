package com.tpe.parking.controller.web;

import com.tpe.parking.dto.request.CreateTarifRequest;
import com.tpe.parking.dto.response.TarifResponse;
import com.tpe.parking.entity.Tarif;
import com.tpe.parking.service.TarifService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

/**
 * Gestion web des tarifs du parking : grille tarifaire par zone, tarification horaire,
 * tarif minimum, forfait journalier, réduction abonné et majoration après dépassement.
 * Réservé au rôle ADMIN pour les créations / suppressions.
 */
@Controller
@RequestMapping("/tarifs")
@RequiredArgsConstructor
public class TarifWebController {

    private final TarifService tarifService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String liste(Model model) {
        List<Tarif> tarifs = tarifService.getAllTarifs();
        model.addAttribute("tarifs", tarifs);
        return "tarifs";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String creer(@Valid @ModelAttribute CreateTarifRequest request,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", FormValidation.message(bindingResult));
            return "redirect:/tarifs";
        }
        try {
            TarifResponse tarif = tarifService.creerTarif(new CreateTarifRequest(
                    request.zone().trim().toUpperCase(),
                    request.montantParHeure(),
                    request.tarifMinimum() != null ? request.tarifMinimum() : BigDecimal.ZERO,
                    request.forfaitJournalier() != null ? request.forfaitJournalier() : BigDecimal.ZERO,
                    request.reductionAbonne() != null ? request.reductionAbonne() : BigDecimal.ZERO,
                    request.majorationApres() != null ? request.majorationApres() : BigDecimal.ZERO));
            redirectAttributes.addFlashAttribute("succes",
                    "Grille tarifaire pour la zone " + tarif.zone() + " créée avec succès.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/tarifs";
    }

    @PostMapping("/{id}/supprimer")
    @PreAuthorize("hasRole('ADMIN')")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tarifService.supprimerTarif(id);
            redirectAttributes.addFlashAttribute("succes", "Tarif #" + id + " supprimé.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/tarifs";
    }
}
