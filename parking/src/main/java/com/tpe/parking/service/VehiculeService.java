package com.tpe.parking.service;

import java.util.List;

import com.tpe.parking.dto.request.CreateVehiculeRequest;
import com.tpe.parking.dto.response.VehiculeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehiculeService {
    List<VehiculeResponse> getAllVehicules();

    /** Recherche paginée sur la plaque, la marque ou le modèle, avec filtre propriétaire. */
    Page<VehiculeResponse> rechercherVehicules(String recherche, Long utilisateurId, Pageable pageable);

    List<VehiculeResponse> getVehiculesByUtilisateurId(Long utilisateurId);

    VehiculeResponse createVehicule(CreateVehiculeRequest request);

    VehiculeResponse updateVehicule(Long id, CreateVehiculeRequest request);

    VehiculeResponse getVehiculeById(Long id);

    void deleteVehicule(Long id);
    List<VehiculeResponse> getVehiculesByImmatriculation(String immatriculation);
}
