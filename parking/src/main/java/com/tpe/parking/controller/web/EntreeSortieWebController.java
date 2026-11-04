package com.tpe.parking.controller.web;

import com.tpe.parking.dto.request.EntreeVehiculeRequest;
import com.tpe.parking.dto.request.SortieVehiculeRequest;
import com.tpe.parking.dto.response.EntreeSortieResponse;
import com.tpe.parking.dto.response.PlaceResponse;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.service.EntreeSortieService;
import com.tpe.parking.service.PlaceService;
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

import java.util.List;
import java.util.Objects;

/**
 * Borne d'entrée / sortie : le gardien saisit une plaque, le système attribue automatiquement
 * une place compatible (réservation, abonnement, zone souhaitée ou substitution) puis facture
 * la sortie en tenant compte des dépassements. Chaque opération est transactionnelle côté service.
 */
@Controller
@RequestMapping("/borne")
@RequiredArgsConstructor
public class EntreeSortieWebController {

    private static final int TAILLE_PAGE = 10;

    private final EntreeSortieService entreeSortieService;
    private final PlaceService placeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String borne(@RequestParam(required = false) Boolean enCours,
                        @RequestParam(required = false) String immatriculation,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<EntreeSortieResponse> historique = entreeSortieService.rechercherEntreesSorties(
                enCours, immatriculation,
                PageRequest.of(Math.max(page, 0), TAILLE_PAGE,
                        Sort.by(Sort.Direction.DESC, "dateEntree")));

        model.addAttribute("page", historique);
        model.addAttribute("enCoursSelectionne", enCours);
        model.addAttribute("immatriculationRecherchee", immatriculation);
        model.addAttribute("placesLibres", placesLibres());
        model.addAttribute("zones", zones());
        model.addAttribute("vehiculesPresents", entreeSortieService.rechercherEntreesSorties(
                true, null, PageRequest.of(0, 100, Sort.by("dateEntree"))).getContent());
        return "borne";
    }

    @PostMapping("/entree")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String enregistrerEntree(@Valid @ModelAttribute EntreeVehiculeRequest request,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", FormValidation.message(bindingResult));
            return "redirect:/borne";
        }
        try {
            EntreeSortieResponse ticket = entreeSortieService.enregistrerEntree(request);
            redirectAttributes.addFlashAttribute("ticket", ticket);
            redirectAttributes.addFlashAttribute("succes",
                    "Entrée enregistrée : " + ticket.immatriculationVehicule()
                            + " → place " + ticket.numeroPlace() + ".");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/borne";
    }

    @PostMapping("/sortie")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String enregistrerSortie(@Valid @ModelAttribute SortieVehiculeRequest request,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", FormValidation.message(bindingResult));
            return "redirect:/borne";
        }
        try {
            EntreeSortieResponse ticket = entreeSortieService.enregistrerSortie(request);
            redirectAttributes.addFlashAttribute("ticket", ticket);
            redirectAttributes.addFlashAttribute("succes",
                    "Sortie enregistrée : " + ticket.immatriculationVehicule()
                            + " — montant facturé " + ticket.montant() + " (place "
                            + ticket.numeroPlace() + " libérée).");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/borne";
    }

    private List<PlaceResponse> placesLibres() {
        return placeService.rechercherPlaces(null, null, PlaceStatut.LIBRE,
                        PageRequest.of(0, 500, Sort.by("zone", "numero")))
                .getContent();
    }

    private List<String> zones() {
        return placeService.getAllPlaces().stream()
                .map(PlaceResponse::zone)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }
}
