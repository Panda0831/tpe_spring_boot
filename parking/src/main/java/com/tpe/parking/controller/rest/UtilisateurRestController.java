package com.tpe.parking.controller.rest;

import com.tpe.parking.dto.request.CreateUtilisateurRequest;
import com.tpe.parking.dto.response.UtilisateurResponse;
import com.tpe.parking.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurRestController {

    private final UtilisateurService utilisateurService;

    /**
     * Liste paginée, triée et filtrable des utilisateurs.
     * Exemple : {@code GET /api/utilisateurs?page=0&size=10&sort=nom,asc&recherche=celine}.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public Page<UtilisateurResponse> getUtilisateurs(
            @RequestParam(required = false) String recherche,
            @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return utilisateurService.rechercherUtilisateurs(recherche, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public UtilisateurResponse getUtilisateur(@PathVariable Long id) {
        return utilisateurService.getUtilisateurById(id);
    }

    @PostMapping
    public ResponseEntity<UtilisateurResponse> createUtilisateur(@Valid @RequestBody CreateUtilisateurRequest request) {
        UtilisateurResponse response = utilisateurService.creerUtilisateur(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public UtilisateurResponse updateUtilisateur(@PathVariable Long id, @Valid @RequestBody UtilisateurResponse utilisateurResponse) {
        return utilisateurService.modifierUtilisateur(id, utilisateurResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        utilisateurService.supprimerUtilisateur(id);
        return ResponseEntity.noContent().build();
    }
}