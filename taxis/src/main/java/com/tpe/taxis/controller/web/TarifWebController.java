package com.tpe.taxis.controller.web;

import com.tpe.taxis.dto.request.UpdateParametreTarifWebRequest;
import com.tpe.taxis.dto.response.ParametreTarifResponse;
import com.tpe.taxis.service.ParametreTarifService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Configuration des tarifs et du moteur d'attribution (coefficients proximité/équité).
 * Réservé à l'administrateur pour la modification, consultable par tous les utilisateurs connectés.
 */
@Controller
@RequestMapping("/tarifs")
@RequiredArgsConstructor
public class TarifWebController {

    private final ParametreTarifService parametreTarifService;

    @GetMapping
    public String index(Model model) {
        List<ParametreTarifResponse> tous = parametreTarifService.getAllParametresTarif();
        ParametreTarifResponse tarif = tous.isEmpty() ? null : tous.get(0);
        model.addAttribute("tarif", tarif);
        return "tarifs";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String modifier(@Valid @ModelAttribute UpdateParametreTarifWebRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/tarifs";
        }
        try {
            parametreTarifService.updateParametreTarif(request.id(), request.toParametreTarifRequest());
            redirectAttributes.addFlashAttribute("succes", "Paramètres de tarification et de dispatch mis à jour.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/tarifs";
    }

    private String messageValidation(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Les paramètres de tarification sont invalides.");
    }
}
