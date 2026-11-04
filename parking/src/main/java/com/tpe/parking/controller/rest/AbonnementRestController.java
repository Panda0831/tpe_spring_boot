package com.tpe.parking.controller.rest;

import com.tpe.parking.dto.request.CreateAbonnementRequest;
import com.tpe.parking.dto.response.AbonnementResponse;
import com.tpe.parking.entity.AbonnementStatut;
import com.tpe.parking.service.AbonnementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
public class AbonnementRestController {

    private final AbonnementService abonnementService;

    /**
     * Liste paginée, triée et filtrée des abonnements.
     * Exemple : {@code GET /api/abonnements?page=0&size=10&sort=dateFin,asc&statut=ACTIF}.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public Page<AbonnementResponse> getAllAbonnements(
            @RequestParam(required = false) AbonnementStatut statut,
            @RequestParam(required = false) Long utilisateurId,
            @RequestParam(required = false) Long vehiculeId,
            @PageableDefault(size = 20, sort = "dateFin", direction = org.springframework.data.domain.Sort.Direction.ASC)
            Pageable pageable) {
        return abonnementService.rechercherAbonnements(statut, utilisateurId, vehiculeId, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public AbonnementResponse getAbonnementById(@PathVariable Long id) {
        return abonnementService.getAbonnementById(id);
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public List<AbonnementResponse> getAbonnementsByUtilisateur(@PathVariable Long utilisateurId) {
        return abonnementService.getAbonnementsByUtilisateur(utilisateurId);
    }

    @GetMapping("/vehicule/{vehiculeId}")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public List<AbonnementResponse> getAbonnementsByVehicule(@PathVariable Long vehiculeId) {
        return abonnementService.getAbonnementsByVehicule(vehiculeId);
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public List<AbonnementResponse> getAbonnementsByStatut(@PathVariable AbonnementStatut statut) {
        return abonnementService.getAbonnementsByStatut(statut);
    }

    @PostMapping
    public ResponseEntity<AbonnementResponse> souscrireAbonnement(@Valid @RequestBody CreateAbonnementRequest request) {
        AbonnementResponse response = abonnementService.souscrireAbonnement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/renouveler")
    public AbonnementResponse renouvelerAbonnement(@PathVariable Long id, @RequestParam LocalDate nouvelleDateFin) {
        return abonnementService.renouvelerAbonnement(id, nouvelleDateFin);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> resilierAbonnement(@PathVariable Long id) {
        abonnementService.resilierAbonnement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/immatriculation/{immatriculation}/actif")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public boolean estAbonneActifByImmatriculation(@PathVariable String immatriculation) {
        return abonnementService.estAbonneActif(immatriculation);
    }

    @GetMapping("/vehicule/{vehiculeId}/actif")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public boolean estAbonneActifByVehicule(@PathVariable Long vehiculeId) {
        return abonnementService.estAbonneActif(vehiculeId);
    }

    @GetMapping("/utilisateur/{utilisateurId}/actif")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public boolean hasActiveAbonnement(@PathVariable Long utilisateurId) {
        return abonnementService.hasActiveAbonnement(utilisateurId);
    }
}
