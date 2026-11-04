package com.tpe.taxis.service;

import java.util.List;

import com.tpe.taxis.dto.request.CreateVehiculeRequest;
import com.tpe.taxis.dto.request.UpdateVehiculeRequest;
import com.tpe.taxis.dto.response.VehiculeResponse;
import com.tpe.taxis.entity.VehiculeStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehiculeService {

    VehiculeResponse createVehicule(CreateVehiculeRequest request);

    VehiculeResponse getVehiculeById(Long id);

    VehiculeResponse getVehiculeByImmatriculation(String immatriculation);

    List<VehiculeResponse> getAllVehicules();

    Page<VehiculeResponse> getAllVehicules(Pageable pageable);

    Page<VehiculeResponse> searchVehicules(String marque, String modele, Pageable pageable);

    List<VehiculeResponse> getVehiculesByStatut(VehiculeStatut statut);

    /** Liste paginée et triée des véhicules ayant le statut demandé. */
    Page<VehiculeResponse> getVehiculesByStatut(VehiculeStatut statut, Pageable pageable);

    List<VehiculeResponse> getVehiculesDisponiblesPourCourse();

    VehiculeResponse updateVehicule(Long id, UpdateVehiculeRequest request);

    VehiculeResponse changeStatut(Long id, VehiculeStatut statut);

    VehiculeResponse affecterChauffeur(Long vehiculeId, Long chauffeurId);

    VehiculeResponse retirerChauffeur(Long vehiculeId);

    void deleteVehicule(Long id);
}
