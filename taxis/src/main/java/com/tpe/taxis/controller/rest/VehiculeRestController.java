package com.tpe.taxis.controller.rest;

import com.tpe.taxis.dto.request.CreateVehiculeRequest;
import com.tpe.taxis.dto.request.UpdateVehiculeRequest;
import com.tpe.taxis.dto.response.VehiculeResponse;
import com.tpe.taxis.entity.VehiculeStatut;
import com.tpe.taxis.service.VehiculeService;
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
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeRestController {

    private final VehiculeService vehiculeService;

    @PostMapping
    public ResponseEntity<VehiculeResponse> createVehicule(@Valid @RequestBody CreateVehiculeRequest request) {
        VehiculeResponse response = vehiculeService.createVehicule(request);
        return ResponseEntity.created(URI.create("/api/vehicules/" + response.id())).body(response);
    }

    @GetMapping
    public Page<VehiculeResponse> getAllVehicules(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return vehiculeService.getAllVehicules(pageable);
    }

    @GetMapping("/all")
    public List<VehiculeResponse> getAllVehiculesList() {
        return vehiculeService.getAllVehicules();
    }

    @GetMapping("/{id}")
    public VehiculeResponse getVehiculeById(@PathVariable Long id) {
        return vehiculeService.getVehiculeById(id);
    }

    @GetMapping("/immatriculation/{immatriculation}")
    public VehiculeResponse getVehiculeByImmatriculation(@PathVariable String immatriculation) {
        return vehiculeService.getVehiculeByImmatriculation(immatriculation);
    }

    @GetMapping("/search")
    public Page<VehiculeResponse> searchVehicules(
            @RequestParam String marque,
            @RequestParam String modele,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return vehiculeService.searchVehicules(marque, modele, pageable);
    }

    @GetMapping("/statut/{statut}")
    public List<VehiculeResponse> getVehiculesByStatut(@PathVariable VehiculeStatut statut) {
        return vehiculeService.getVehiculesByStatut(statut);
    }

    @GetMapping("/disponibles")
    public List<VehiculeResponse> getVehiculesDisponiblesPourCourse() {
        return vehiculeService.getVehiculesDisponiblesPourCourse();
    }

    @PutMapping("/{id}")
    public VehiculeResponse updateVehicule(@PathVariable Long id,
                                           @Valid @RequestBody UpdateVehiculeRequest request) {
        return vehiculeService.updateVehicule(id, request);
    }

    @PatchMapping("/{id}/statut")
    public VehiculeResponse changeStatut(@PathVariable Long id, @RequestParam VehiculeStatut statut) {
        return vehiculeService.changeStatut(id, statut);
    }

    @PatchMapping("/{id}/chauffeur/{chauffeurId}")
    public VehiculeResponse affecterChauffeur(@PathVariable Long id, @PathVariable Long chauffeurId) {
        return vehiculeService.affecterChauffeur(id, chauffeurId);
    }

    @DeleteMapping("/{id}/chauffeur")
    public VehiculeResponse retirerChauffeur(@PathVariable Long id) {
        return vehiculeService.retirerChauffeur(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        vehiculeService.deleteVehicule(id);
        return ResponseEntity.noContent().build();
    }
}
