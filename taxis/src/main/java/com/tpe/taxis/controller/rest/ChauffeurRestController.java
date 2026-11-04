package com.tpe.taxis.controller.rest;

import com.tpe.taxis.dto.request.CreateChauffeurRequest;
import com.tpe.taxis.dto.request.UpdateChauffeurRequest;
import com.tpe.taxis.dto.response.ChauffeurResponse;
import com.tpe.taxis.entity.ChauffeurStatut;
import com.tpe.taxis.service.ChauffeurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/chauffeurs")
@RequiredArgsConstructor
public class ChauffeurRestController {

    private final ChauffeurService chauffeurService;

    @PostMapping
    public ResponseEntity<ChauffeurResponse> createChauffeur(@Valid @RequestBody CreateChauffeurRequest request) {
        ChauffeurResponse response = chauffeurService.createChauffeur(request);
        return ResponseEntity.created(URI.create("/api/chauffeurs/" + response.id())).body(response);
    }

    @GetMapping
    public Page<ChauffeurResponse> getAllChauffeurs(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return chauffeurService.getAllChauffeurs(pageable);
    }

    @GetMapping("/all")
    public List<ChauffeurResponse> getAllChauffeursList() {
        return chauffeurService.getAllChauffeurs();
    }

    @GetMapping("/{id}")
    public ChauffeurResponse getChauffeurById(@PathVariable Long id) {
        return chauffeurService.getChauffeurById(id);
    }

    @GetMapping("/permis/{numeroPermis}")
    public ChauffeurResponse getChauffeurByPermis(@PathVariable String numeroPermis) {
        return chauffeurService.getChauffeurByNumeroPermis(numeroPermis);
    }

    @GetMapping("/search")
    public Page<ChauffeurResponse> searchChauffeurs(
            @RequestParam String nom,
            @RequestParam String prenom,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return chauffeurService.searchChauffeurs(nom, prenom, pageable);
    }

    @GetMapping("/statut/{statut}")
    public List<ChauffeurResponse> getChauffeursByStatut(@PathVariable ChauffeurStatut statut) {
        return chauffeurService.getChauffeursByStatut(statut);
    }

    @GetMapping("/eligibles")
    public List<ChauffeurResponse> getChauffeursEligiblesPourCourse() {
        return chauffeurService.getChauffeursEligiblesPourCourse();
    }

    @PutMapping("/{id}")
    public ChauffeurResponse updateChauffeur(@PathVariable Long id,
                                             @Valid @RequestBody UpdateChauffeurRequest request) {
        return chauffeurService.updateChauffeur(id, request);
    }

    @PatchMapping("/{id}/statut")
    public ChauffeurResponse changeStatut(@PathVariable Long id, @RequestParam ChauffeurStatut statut) {
        return chauffeurService.changeStatut(id, statut);
    }

    @PatchMapping("/{id}/vehicule/{vehiculeId}")
    public ChauffeurResponse affecterVehicule(@PathVariable Long id, @PathVariable Long vehiculeId) {
        return chauffeurService.affecterVehicule(id, vehiculeId);
    }

    @DeleteMapping("/{id}/vehicule")
    public ChauffeurResponse retirerVehicule(@PathVariable Long id) {
        return chauffeurService.retirerVehicule(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChauffeur(@PathVariable Long id) {
        chauffeurService.deleteChauffeur(id);
        return ResponseEntity.noContent().build();
    }
}
