package com.tpe.parking.controller.web;

import com.tpe.parking.dto.request.CreatePlaceRequest;
import com.tpe.parking.dto.response.PlaceResponse;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.PlaceType;
import com.tpe.parking.exception.ConflictException;
import com.tpe.parking.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Plan interactif du parking : grille colorée des places (LIBRE / OCCUPEE / RESERVEE / HORS_SERVICE)
 * groupée par zone, avec filtrage par zone, type et statut.
 */
@Controller
@RequiredArgsConstructor
public class PlaceWebController {

    /** Un plan doit rester lisible en une seule vue : on affiche toutes les places filtrées. */
    private static final int TAILLE_PLAN = 500;

    private final PlaceService placeService;

    @GetMapping("/places")
    @PreAuthorize("hasAnyRole('USER','OPERATOR','MANAGER','ADMIN')")
    public String places(@RequestParam(required = false) String zone,
                         @RequestParam(required = false) PlaceType type,
                         @RequestParam(required = false) PlaceStatut statut,
                         Model model) {
        List<PlaceResponse> filtrees = placeService.rechercherPlaces(
                        zone, type, statut,
                        PageRequest.of(0, TAILLE_PLAN, Sort.by("zone", "numero")))
                .getContent();

        Map<String, List<PlaceResponse>> placesParZone = new LinkedHashMap<>();
        for (PlaceResponse place : filtrees) {
            String cle = place.zone() == null ? "—" : place.zone();
            placesParZone.computeIfAbsent(cle, k -> new java.util.ArrayList<>()).add(place);
        }

        List<String> zones = placeService.getAllPlaces().stream()
                .map(PlaceResponse::zone)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("occupationLibres", compter(filtrees, PlaceStatut.LIBRE));
        model.addAttribute("occupationOccupees", compter(filtrees, PlaceStatut.OCCUPEE));
        model.addAttribute("occupationReservees", compter(filtrees, PlaceStatut.RESERVEE));
        model.addAttribute("places", filtrees);
        model.addAttribute("placesParZone", placesParZone);
        model.addAttribute("zones", zones);
        model.addAttribute("types", PlaceType.values());
        model.addAttribute("statuts", PlaceStatut.values());
        model.addAttribute("zoneSelectionnee", zone);
        model.addAttribute("typeSelectionne", type);
        model.addAttribute("statutSelectionne", statut);
        return "places";
    }

    @PostMapping("/places")
    @PreAuthorize("hasRole('ADMIN')")
    public String creer(@Valid @ModelAttribute CreatePlaceRequest request,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", FormValidation.message(bindingResult));
            return "redirect:/places";
        }
        try {
            PlaceResponse place = placeService.creerPlace(request);
            redirectAttributes.addFlashAttribute("succes",
                    "Place " + place.numero() + " créée dans la zone " + place.zone() + ".");
        } catch (ConflictException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/places";
    }

    private long compter(List<PlaceResponse> places, PlaceStatut statut) {
        return places.stream().filter(p -> p.statut() == statut).count();
    }
}
