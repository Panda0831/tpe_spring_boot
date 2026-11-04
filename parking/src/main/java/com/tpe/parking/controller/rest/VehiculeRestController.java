package com.tpe.parking.controller.rest;

import java.util.List;

import com.tpe.parking.dto.request.CreateVehiculeRequest;
import com.tpe.parking.dto.response.VehiculeResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeRestController {

    private final VehiculeService vehiculeService;

    /**
     * Liste paginée, triée et filtrable des véhicules.
     * Exemple : {@code GET /api/vehicules?page=0&size=10&sort=immatriculation,asc&recherche=TOYOTA}.
     */
    @GetMapping
    public Page<VehiculeResponse> getAllVehicules(
            @AuthenticationPrincipal UtilisateurPrincipal principal,
            @RequestParam(required = false) String recherche,
            @RequestParam(required = false) Long utilisateurId,
            @PageableDefault(size = 20, sort = "immatriculation") Pageable pageable) {
        Long proprietaire = UtilisateurAccess.verifierFiltreUtilisateur(principal, utilisateurId);
        return vehiculeService.rechercherVehicules(recherche, proprietaire, pageable);
    }

    @GetMapping("/{id}")
    public VehiculeResponse getVehiculeById(@PathVariable Long id,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        VehiculeResponse vehicule = vehiculeService.getVehiculeById(id);
        UtilisateurAccess.verifierProprietaire(principal, vehicule.utilisateurId());
        return vehicule;
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<VehiculeResponse> getVehiculesByUtilisateurId(@PathVariable Long utilisateurId,
            @AuthenticationPrincipal UtilisateurPrincipal principal) {
        UtilisateurAccess.verifierProprietaire(principal, utilisateurId);
        return vehiculeService.getVehiculesByUtilisateurId(utilisateurId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<VehiculeResponse> createVehicule(@Valid @RequestBody CreateVehiculeRequest request) {
        VehiculeResponse response = vehiculeService.createVehicule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public VehiculeResponse updateVehicule(@PathVariable Long id, @Valid @RequestBody CreateVehiculeRequest request) {
        return vehiculeService.updateVehicule(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        vehiculeService.deleteVehicule(id);
        return ResponseEntity.noContent().build();
    }
}
