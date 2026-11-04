package com.tpe.parking.controller.web;

import com.tpe.parking.dto.request.CreateReservationRequest;
import com.tpe.parking.dto.response.PlaceResponse;
import com.tpe.parking.dto.response.ReservationResponse;
import com.tpe.parking.dto.response.VehiculeResponse;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.ReservationStatus;
import com.tpe.parking.service.PlaceService;
import com.tpe.parking.service.ReservationService;
import com.tpe.parking.service.UtilisateurService;
import com.tpe.parking.service.VehiculeService;
import com.tpe.parking.security.CustomUserDetailsService.UtilisateurPrincipal;
import com.tpe.parking.security.UtilisateurAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Gestion des réservations : consultation paginée et filtrée, création (réservation de place)
 * et annulation. Toutes les actions passent par la même couche métier que l'API REST.
 */
@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationWebController {

    private static final int TAILLE_PAGE = 10;

    private final ReservationService reservationService;
    private final UtilisateurService utilisateurService;
    private final PlaceService placeService;
    private final VehiculeService vehiculeService;

    @GetMapping
    public String liste(@RequestParam(required = false) ReservationStatus statut,
                        @RequestParam(defaultValue = "0") int page,
                        @AuthenticationPrincipal UtilisateurPrincipal principal,
                        Model model) {
        Long utilisateurId = UtilisateurAccess.estUtilisateur(principal)
                ? UtilisateurAccess.id(principal) : null;
        Page<ReservationResponse> resultats = reservationService.rechercherReservations(
                statut, utilisateurId, null, null,
                PageRequest.of(Math.max(page, 0), TAILLE_PAGE,
                        Sort.by(Sort.Direction.DESC, "dateArriveePrevue")));

        model.addAttribute("page", resultats);
        model.addAttribute("statuts", ReservationStatus.values());
        model.addAttribute("statutSelectionne", statut);
        preparerFormulaire(model, principal);
        return "reservations";
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute CreateReservationRequest request,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal UtilisateurPrincipal principal,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", FormValidation.message(bindingResult));
            return "redirect:/reservations";
        }
        try {
            Long utilisateurId = UtilisateurAccess.verifierFiltreUtilisateur(principal, request.utilisateurId());
            CreateReservationRequest reservation = UtilisateurAccess.estUtilisateur(principal)
                    ? new CreateReservationRequest(utilisateurId, request.vehiculeId(), request.placeId(),
                            request.dateArriveePrevue(), request.dateDepartPrevue())
                    : request;
            ReservationResponse created = reservationService.createReservation(reservation);
            redirectAttributes.addFlashAttribute("succes",
                    "Réservation #" + created.id() + " créée"
                            + (created.numeroPlace() != null ? " sur la place " + created.numeroPlace() : "")
                            + ".");
        } catch (AccessDeniedException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/reservations";
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.annulerReservation(id);
            redirectAttributes.addFlashAttribute("succes", "Réservation #" + id + " annulée.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/reservations";
    }

    private void preparerFormulaire(Model model, UtilisateurPrincipal principal) {
        boolean utilisateur = UtilisateurAccess.estUtilisateur(principal);
        Long utilisateurId = utilisateur ? UtilisateurAccess.id(principal) : null;
        model.addAttribute("utilisateurPropre", utilisateur);
        model.addAttribute("utilisateurIdConnecte", utilisateurId);
        model.addAttribute("utilisateurs", utilisateur ? List.of() : utilisateurService.getAllUtilisateurs());
        model.addAttribute("vehicules", utilisateur
                ? vehiculeService.getVehiculesByUtilisateurId(utilisateurId)
                : vehiculeService.getAllVehicules());
        List<PlaceResponse> placesLibres = placeService.getAllPlaces().stream()
                .filter(p -> p.statut() == PlaceStatut.LIBRE)
                .toList();
        model.addAttribute("placesLibres", placesLibres);
    }
}
