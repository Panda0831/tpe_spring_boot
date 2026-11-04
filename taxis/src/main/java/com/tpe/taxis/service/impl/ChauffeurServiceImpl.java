package com.tpe.taxis.service.impl;

import java.util.List;

import com.tpe.taxis.dto.request.CreateChauffeurRequest;
import com.tpe.taxis.dto.request.UpdateChauffeurRequest;
import com.tpe.taxis.dto.response.ChauffeurResponse;
import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.ChauffeurStatut;
import com.tpe.taxis.entity.Vehicule;
import com.tpe.taxis.entity.VehiculeStatut;
import com.tpe.taxis.exception.ChauffeurIndisponibleException;
import com.tpe.taxis.exception.ConflictException;
import com.tpe.taxis.exception.ResourceNotFoundException;
import com.tpe.taxis.exception.VehiculeEnMaintenanceException;
import com.tpe.taxis.mapper.ChauffeurMapper;
import com.tpe.taxis.repository.ChauffeurRepository;
import com.tpe.taxis.repository.VehiculeRepository;
import com.tpe.taxis.service.ChauffeurService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChauffeurServiceImpl implements ChauffeurService {

    private static final String CHAUFFEUR_NOT_FOUND = "Chauffeur";

    private final ChauffeurRepository chauffeurRepository;
    private final VehiculeRepository vehiculeRepository;

    @Override
    public ChauffeurResponse createChauffeur(CreateChauffeurRequest request) {
        String numeroPermis = request.numeroPermis().trim().toUpperCase();
        if (chauffeurRepository.existsByNumeroPermis(numeroPermis)) {
            throw new ConflictException("Un chauffeur avec le numéro de permis " + numeroPermis + " existe déjà.");
        }

        Chauffeur chauffeur = ChauffeurMapper.toEntity(request);

        if (request.vehiculeId() != null) {
            Vehicule vehicule = getVehiculeEligible(request.vehiculeId());
            vehicule.setChauffeurActuel(chauffeur);
            chauffeur.setVehicule(vehicule);
        }

        return ChauffeurMapper.toResponse(chauffeurRepository.save(chauffeur));
    }

    @Override
    @Transactional(readOnly = true)
    public ChauffeurResponse getChauffeurById(Long id) {
        return ChauffeurMapper.toResponse(getChauffeurEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ChauffeurResponse getChauffeurByNumeroPermis(String numeroPermis) {
        String permis = numeroPermis != null ? numeroPermis.trim().toUpperCase() : null;
        return chauffeurRepository.findByNumeroPermis(permis)
                .map(ChauffeurMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Chauffeur", "numéro de permis", numeroPermis));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChauffeurResponse> getAllChauffeurs() {
        return chauffeurRepository.findAll()
                .stream()
                .map(ChauffeurMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChauffeurResponse> getAllChauffeurs(Pageable pageable) {
        return chauffeurRepository.findAll(pageable)
                .map(ChauffeurMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChauffeurResponse> searchChauffeurs(String nom, String prenom, Pageable pageable) {
        return chauffeurRepository
                .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(nom, prenom, pageable)
                .map(ChauffeurMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChauffeurResponse> getChauffeursByStatut(ChauffeurStatut statut) {
        return chauffeurRepository.findByStatut(statut)
                .stream()
                .map(ChauffeurMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChauffeurResponse> getChauffeursByStatut(ChauffeurStatut statut, Pageable pageable) {
        return chauffeurRepository.findByStatut(statut, pageable)
                .map(ChauffeurMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChauffeurResponse> getChauffeursEligiblesPourCourse() {
        return chauffeurRepository.findEligiblesPourCourse(java.time.LocalDate.now())
                .stream()
                .map(ChauffeurMapper::toResponse)
                .toList();
    }

    @Override
    public ChauffeurResponse updateChauffeur(Long id, UpdateChauffeurRequest request) {
        Chauffeur chauffeur = getChauffeurEntity(id);

        if (request.numeroPermis() != null && !request.numeroPermis().isBlank()) {
            String numeroPermis = request.numeroPermis().trim().toUpperCase();
            if (!numeroPermis.equals(chauffeur.getNumeroPermis())
                    && chauffeurRepository.existsByNumeroPermis(numeroPermis)) {
                throw new ConflictException(
                        "Un chauffeur avec le numéro de permis " + numeroPermis + " existe déjà.");
            }
        }

        ChauffeurMapper.updateEntity(chauffeur, request);
        return ChauffeurMapper.toResponse(chauffeurRepository.save(chauffeur));
    }

    @Override
    public ChauffeurResponse changeStatut(Long id, ChauffeurStatut statut) {
        Chauffeur chauffeur = getChauffeurEntity(id);
        chauffeur.setStatut(statut);
        return ChauffeurMapper.toResponse(chauffeurRepository.save(chauffeur));
    }

    @Override
    public ChauffeurResponse affecterVehicule(Long chauffeurId, Long vehiculeId) {
        Chauffeur chauffeur = getChauffeurEntity(chauffeurId);
        Vehicule vehicule = getVehiculeEligible(vehiculeId);

        if (vehicule.getChauffeurActuel() != null
                && !vehicule.getChauffeurActuel().getId().equals(chauffeurId)) {
            throw new ConflictException(
                    "Le véhicule " + vehicule.getImmatriculation() + " est déjà affecté à un autre chauffeur.");
        }

        chauffeur.setVehicule(vehicule);
        vehicule.setChauffeurActuel(chauffeur);
        vehiculeRepository.save(vehicule);

        return ChauffeurMapper.toResponse(chauffeurRepository.save(chauffeur));
    }

    @Override
    public ChauffeurResponse retirerVehicule(Long chauffeurId) {
        Chauffeur chauffeur = getChauffeurEntity(chauffeurId);
        Vehicule vehicule = chauffeur.getVehicule();
        if (vehicule != null) {
            vehicule.setChauffeurActuel(null);
            vehiculeRepository.save(vehicule);
            chauffeur.setVehicule(null);
        }
        return ChauffeurMapper.toResponse(chauffeurRepository.save(chauffeur));
    }

    @Override
    public void deleteChauffeur(Long id) {
        Chauffeur chauffeur = getChauffeurEntity(id);
        if (chauffeur.getStatut() == ChauffeurStatut.EN_COURSE) {
            throw new ConflictException(
                    "Impossible de supprimer le chauffeur " + chauffeur.getPrenom() + " " + chauffeur.getNom()
                            + " car il est actuellement en course.");
        }

        Vehicule vehicule = chauffeur.getVehicule();
        if (vehicule != null) {
            vehicule.setChauffeurActuel(null);
            vehiculeRepository.save(vehicule);
        }

        chauffeurRepository.delete(chauffeur);
    }

    private Chauffeur getChauffeurEntity(Long id) {
        return chauffeurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CHAUFFEUR_NOT_FOUND, id));
    }

    private Vehicule getVehiculeEligible(Long vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", vehiculeId));
        if (vehicule.getStatut() == VehiculeStatut.MAINTENANCE
                || vehicule.getStatut() == VehiculeStatut.HORS_SERVICE) {
            throw new VehiculeEnMaintenanceException(
                    vehicule.getId(), vehicule.getImmatriculation(), vehicule.getStatut());
        }
        return vehicule;
    }
}
