package com.tpe.parking.service;

import com.tpe.parking.dto.request.CreateUtilisateurRequest;
import com.tpe.parking.dto.response.UtilisateurResponse;
import com.tpe.parking.dto.response.VehiculeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UtilisateurService {

    @Transactional(readOnly = true)
    List<VehiculeResponse> getVehiculesByUtilisateurId(Long utilisateurId);

    UtilisateurResponse creerUtilisateur(CreateUtilisateurRequest request);

    UtilisateurResponse getUtilisateurById(Long id);

    List<UtilisateurResponse> getAllUtilisateurs();

    /** Recherche paginée sur le nom complet ou l'email. */
    Page<UtilisateurResponse> rechercherUtilisateurs(String recherche, Pageable pageable);

    UtilisateurResponse modifierUtilisateur(Long id, UtilisateurResponse utilisateurResponse);
    void supprimerUtilisateur(Long id);


}
