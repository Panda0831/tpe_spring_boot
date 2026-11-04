package com.tpe.parking.controller.rest;

import com.tpe.parking.dto.request.EntreeVehiculeRequest;
import com.tpe.parking.dto.request.SortieVehiculeRequest;
import com.tpe.parking.dto.response.EntreeSortieResponse;
import com.tpe.parking.service.EntreeSortieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST des passages à la borne. Aucune entité JPA n'est exposée : uniquement des DTOs.
 */
@RestController
@RequestMapping("/api/entrees-sorties")
@RequiredArgsConstructor
public class EntreeSortieRestController {

    private final EntreeSortieService entreeSortieService;

    /**
     * Historique paginé, trié et filtré des passages à la borne.
     * Exemple : {@code GET /api/entrees-sorties?page=0&size=10&sort=dateEntree,desc&enCours=true}.
     */
    @GetMapping
    public Page<EntreeSortieResponse> getAllEntreesSorties(
            @RequestParam(required = false) Boolean enCours,
            @RequestParam(required = false) String immatriculation,
            @PageableDefault(size = 20, sort = "dateEntree", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return entreeSortieService.rechercherEntreesSorties(enCours, immatriculation, pageable);
    }

    @GetMapping("/{id}")
    public EntreeSortieResponse getEntreeSortieById(@PathVariable Long id) {
        return entreeSortieService.getEntreeSortieById(id);
    }

    @PostMapping("/entree")
    public ResponseEntity<EntreeSortieResponse> enregistrerEntree(
            @Valid @RequestBody EntreeVehiculeRequest request) {
        EntreeSortieResponse response = entreeSortieService.enregistrerEntree(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sortie")
    public EntreeSortieResponse enregistrerSortie(@Valid @RequestBody SortieVehiculeRequest request) {
        return entreeSortieService.enregistrerSortie(request);
    }
}
