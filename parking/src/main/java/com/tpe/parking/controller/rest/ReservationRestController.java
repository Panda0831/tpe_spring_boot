package com.tpe.parking.controller.rest;

import com.tpe.parking.dto.request.CreateReservationRequest;
import com.tpe.parking.dto.request.UpdateReservationRequest;
import com.tpe.parking.dto.response.ReservationResponse;
import com.tpe.parking.entity.ReservationStatus;
import com.tpe.parking.service.ReservationService;
import com.tpe.parking.service.AbonnementService;
import com.tpe.parking.service.VehiculeService;
import com.tpe.parking.security.CustomUserDetailsService.UtilisateurPrincipal;
import com.tpe.parking.security.UtilisateurAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationRestController {

    private final ReservationService reservationService;
    private final AbonnementService abonnementService;
    private final VehiculeService vehiculeService;

    /**
     * Liste paginée, triée et filtrée des réservations.
     * Exemple : {@code GET /api/reservations?page=0&size=10&sort=dateArriveePrevue,desc&statut=EN_ATTENTE}.
     */
    @GetMapping
    public Page<ReservationResponse> getReservations(
            @AuthenticationPrincipal UtilisateurPrincipal principal,
            @RequestParam(required = false) ReservationStatus statut,
            @RequestParam(required = false) Long utilisateurId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) Long placeId,
            @PageableDefault(size = 20, sort = "dateArriveePrevue", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        Long proprietaire = UtilisateurAccess.verifierFiltreUtilisateur(principal, utilisateurId);
        Page<ReservationResponse> page = reservationService.rechercherReservations(
                statut, proprietaire, vehiculeId, placeId, pageable);
        if (UtilisateurAccess.estUtilisateur(principal)) {
            return page.map(reservation -> UtilisateurAccess.verifierProprietaire(principal, reservation));
        }
        return page;
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservationById(@PathVariable Long id,
                                                   @AuthenticationPrincipal UtilisateurPrincipal principal) {
        return UtilisateurAccess.verifierProprietaire(principal, reservationService.getReservationById(id));
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<ReservationResponse> getReservationsByUtilisateur(@PathVariable Long utilisateurId,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        UtilisateurAccess.verifierProprietaire(principal, utilisateurId);
        return reservationService.getReservationsByUtilisateur(utilisateurId);
    }

    @GetMapping("/vehicule/{vehiculeId}")
    public List<ReservationResponse> getReservationsByVehicule(@PathVariable Long vehiculeId,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        UtilisateurAccess.verifierProprietaire(principal,
                vehiculeService.getVehiculeById(vehiculeId).utilisateurId());
        return UtilisateurAccess.filtrerReservationsUtilisateur(
                principal, reservationService.getReservationsByVehicule(vehiculeId));
    }

    @GetMapping("/place/{placeId}")
    public List<ReservationResponse> getReservationsByPlace(@PathVariable Long placeId,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        return UtilisateurAccess.filtrerReservationsUtilisateur(
                principal, reservationService.getReservationsByPlace(placeId));
    }

    @GetMapping("/statut/{statut}")
    public List<ReservationResponse> getReservationsByStatut(@PathVariable ReservationStatus statut,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        return UtilisateurAccess.filtrerReservationsUtilisateur(
                principal, reservationService.getReservationsByStatut(statut));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        Long ownerId = UtilisateurAccess.verifierFiltreUtilisateur(principal, request.utilisateurId());
        CreateReservationRequest reservation = UtilisateurAccess.estUtilisateur(principal)
                ? new CreateReservationRequest(ownerId, request.vehiculeId(), request.placeId(),
                        request.dateArriveePrevue(), request.dateDepartPrevue())
                : request;
        ReservationResponse response = reservationService.createReservation(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ReservationResponse updateReservation(@PathVariable Long id, @Valid @RequestBody UpdateReservationRequest request) {
        return reservationService.updateReservation(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annulerReservation(@PathVariable Long id) {
        reservationService.annulerReservation(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/supprimer")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/utilisateur/{utilisateurId}/abonnement-actif")
    public boolean hasActiveAbonnement(@PathVariable Long utilisateurId,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        UtilisateurAccess.verifierProprietaire(principal, utilisateurId);
        return abonnementService.hasActiveAbonnement(utilisateurId);
    }
}
