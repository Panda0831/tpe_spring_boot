package com.tpe.parking.controller.rest;

import com.tpe.parking.dto.request.CreatePlaceRequest;
import com.tpe.parking.dto.response.PlaceResponse;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.PlaceType;
import com.tpe.parking.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceRestController {

    private final PlaceService placeService;

    /**
     * Liste paginée, triée et filtrée des places.
     * Exemple : {@code GET /api/places?page=0&size=10&sort=numero,desc&statut=LIBRE&zone=RDC}.
     */
    @GetMapping
    public Page<PlaceResponse> getAllPlaces(@RequestParam(required = false) String zone,
                                            @RequestParam(required = false) PlaceType type,
                                            @RequestParam(required = false) PlaceStatut statut,
                                            @PageableDefault(size = 24, sort = "numero") Pageable pageable) {
        return placeService.rechercherPlaces(zone, type, statut, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlaceResponse> creerPlace(@Valid @RequestBody CreatePlaceRequest request) {
        PlaceResponse place = placeService.creerPlace(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(place);
    }

    @PostMapping("/assigner")
    public ResponseEntity<Void> assignerPlaceParking(
            @RequestParam Long utilisateurId,
            @RequestParam Long vehiculeId) {
        placeService.assignerPlaceParking(utilisateurId, vehiculeId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/liberer")
    public ResponseEntity<Void> libererPlaceParking(
            @RequestParam Long utilisateurId,
            @RequestParam Long vehiculeId) {
        placeService.libererPlaceParking(utilisateurId, vehiculeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transferer")
    public ResponseEntity<Void> transfererPlaceParking(
            @RequestParam Long utilisateurSourceId,
            @RequestParam Long utilisateurDestinationId,
            @RequestParam Long vehiculeId) {
        placeService.transfererPlaceParking(utilisateurSourceId, utilisateurDestinationId, vehiculeId);
        return ResponseEntity.ok().build();
    }
}
