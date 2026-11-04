package com.tpe.taxis.service;

import java.util.List;

import com.tpe.taxis.dto.request.CreateChauffeurRequest;
import com.tpe.taxis.dto.request.UpdateChauffeurRequest;
import com.tpe.taxis.dto.response.ChauffeurResponse;
import com.tpe.taxis.entity.ChauffeurStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChauffeurService {

    ChauffeurResponse createChauffeur(CreateChauffeurRequest request);

    ChauffeurResponse getChauffeurById(Long id);

    ChauffeurResponse getChauffeurByNumeroPermis(String numeroPermis);

    List<ChauffeurResponse> getAllChauffeurs();

    Page<ChauffeurResponse> getAllChauffeurs(Pageable pageable);

    Page<ChauffeurResponse> searchChauffeurs(String nom, String prenom, Pageable pageable);

    List<ChauffeurResponse> getChauffeursByStatut(ChauffeurStatut statut);

    /** Liste paginée et triée des chauffeurs ayant le statut demandé. */
    Page<ChauffeurResponse> getChauffeursByStatut(ChauffeurStatut statut, Pageable pageable);

    List<ChauffeurResponse> getChauffeursEligiblesPourCourse();

    ChauffeurResponse updateChauffeur(Long id, UpdateChauffeurRequest request);

    ChauffeurResponse changeStatut(Long id, ChauffeurStatut statut);

    ChauffeurResponse affecterVehicule(Long chauffeurId, Long vehiculeId);

    ChauffeurResponse retirerVehicule(Long chauffeurId);

    void deleteChauffeur(Long id);
}
