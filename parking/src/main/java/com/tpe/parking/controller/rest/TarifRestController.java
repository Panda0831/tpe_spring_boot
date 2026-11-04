package com.tpe.parking.controller.rest;

import com.tpe.parking.dto.request.CreateTarifRequest;
import com.tpe.parking.dto.request.UpdateTarifRequest;
import com.tpe.parking.dto.response.TarifResponse;
import com.tpe.parking.mapper.TarifMapper;
import com.tpe.parking.service.TarifService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * API REST de configuration des tarifs. Aucune entité JPA n'est exposée : uniquement des DTOs.
 * Les écritures sont réservées au rôle ADMIN par la matrice {@code SecurityPaths}.
 */
@RestController
@RequestMapping("/api/tarifs")
@RequiredArgsConstructor
public class TarifRestController {

    private final TarifService tarifService;

    @GetMapping
    public List<TarifResponse> getAllTarifs() {
        return tarifService.getAllTarifs().stream()
                .map(TarifMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public TarifResponse getTarifById(@PathVariable Long id) {
        return tarifService.getTarifById(id);
    }

    @GetMapping("/applicable")
    public TarifResponse getTarifApplicable(@RequestParam String zone) {
        return TarifMapper.toResponse(tarifService.getTarifApplicable(zone));
    }

    @GetMapping("/total")
    public long getNombreTotalTarifs() {
        return tarifService.getNombreTotalTarifs();
    }

    @PostMapping
    public ResponseEntity<TarifResponse> creerTarif(@Valid @RequestBody CreateTarifRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tarifService.creerTarif(request));
    }

    @PutMapping("/{id}")
    public TarifResponse modifierTarif(@PathVariable Long id, @Valid @RequestBody UpdateTarifRequest request) {
        return tarifService.modifierTarif(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerTarif(@PathVariable Long id) {
        tarifService.supprimerTarif(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/calculer-montant")
    public BigDecimal calculerMontant(
            @RequestParam LocalDateTime dateEntree,
            @RequestParam LocalDateTime dateSortie,
            @RequestParam String zone,
            @RequestParam boolean abonne,
            @RequestParam String role,
            @RequestParam String typeVehicule) {
        return tarifService.calculerMontant(dateEntree, dateSortie, zone, abonne,
                com.tpe.parking.entity.Role.valueOf(role), typeVehicule);
    }

    @PostMapping("/calculer-montant-depassement")
    public BigDecimal calculerMontantAvecDepassement(
            @RequestParam LocalDateTime dateEntree,
            @RequestParam LocalDateTime dateSortie,
            @RequestParam LocalDateTime dateDepartPrevue,
            @RequestParam String zone,
            @RequestParam boolean abonne,
            @RequestParam String role,
            @RequestParam String typeVehicule) {
        return tarifService.calculerMontantAvecDepassement(dateEntree, dateSortie, dateDepartPrevue, zone,
                abonne, com.tpe.parking.entity.Role.valueOf(role), typeVehicule);
    }
}
