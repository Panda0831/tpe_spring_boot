package com.tpe.taxis.service.impl;

import java.util.List;

import com.tpe.taxis.dto.request.CreateVehiculeRequest;
import com.tpe.taxis.dto.request.UpdateVehiculeRequest;
import com.tpe.taxis.dto.response.VehiculeResponse;
import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.ChauffeurStatut;
import com.tpe.taxis.entity.Vehicule;
import com.tpe.taxis.entity.VehiculeStatut;
import com.tpe.taxis.exception.ChauffeurIndisponibleException;
import com.tpe.taxis.exception.ConflictException;
import com.tpe.taxis.exception.PermisExpireException;
import com.tpe.taxis.exception.ResourceNotFoundException;
import com.tpe.taxis.exception.VehiculeEnMaintenanceException;
import com.tpe.taxis.mapper.VehiculeMapper;
import com.tpe.taxis.repository.ChauffeurRepository;
import com.tpe.taxis.repository.VehiculeRepository;
import com.tpe.taxis.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VehiculeServiceImpl implements VehiculeService {

    private static final String VEHICULE_NOT_FOUND = "Vehicule";

    private final VehiculeRepository vehiculeRepository;
    private final ChauffeurRepository chauffeurRepository;

    @Override
    public VehiculeResponse createVehicule(CreateVehiculeRequest request) {
        String immatriculation = request.immatriculation().trim().toUpperCase();
        if (vehiculeRepository.existsByImmatriculation(immatriculation)) {
            throw new ConflictException("Un véhicule avec l'immatriculation " + immatriculation + " existe déjà.");
        }

        Chauffeur chauffeur = resolveChauffeur(request.chauffeurId());

        Vehicule vehicule = VehiculeMapper.toEntity(request, chauffeur);
        return VehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeResponse getVehiculeById(Long id) {
        return VehiculeMapper.toResponse(getVehiculeEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeResponse getVehiculeByImmatriculation(String immatriculation) {
        String immat = immatriculation != null ? immatriculation.trim().toUpperCase() : null;
        return vehiculeRepository.findByImmatriculation(immat)
                .map(VehiculeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicule", "immatriculation", immatriculation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> getAllVehicules() {
        return vehiculeRepository.findAll()
                .stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculeResponse> getAllVehicules(Pageable pageable) {
        return vehiculeRepository.findAll(pageable)
                .map(VehiculeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculeResponse> searchVehicules(String marque, String modele, Pageable pageable) {
        return vehiculeRepository
                .findByMarqueContainingIgnoreCaseOrModeleContainingIgnoreCase(marque, modele, pageable)
                .map(VehiculeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> getVehiculesByStatut(VehiculeStatut statut) {
        return vehiculeRepository.findByStatut(statut)
                .stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculeResponse> getVehiculesByStatut(VehiculeStatut statut, Pageable pageable) {
        return vehiculeRepository.findByStatut(statut, pageable)
                .map(VehiculeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> getVehiculesDisponiblesPourCourse() {
        return vehiculeRepository.findDisponiblesPourCourse()
                .stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    @Override
    public VehiculeResponse updateVehicule(Long id, UpdateVehiculeRequest request) {
        Vehicule vehicule = getVehiculeEntity(id);

        if (request.immatriculation() != null && !request.immatriculation().isBlank()) {
            String immatriculation = request.immatriculation().trim().toUpperCase();
            if (!immatriculation.equals(vehicule.getImmatriculation())
                    && vehiculeRepository.existsByImmatriculation(immatriculation)) {
                throw new ConflictException(
                        "Un véhicule avec l'immatriculation " + immatriculation + " existe déjà.");
            }
        }

        Chauffeur chauffeur = resolveChauffeur(request.chauffeurId());
        VehiculeMapper.updateEntity(vehicule, request, chauffeur);

        return VehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    public VehiculeResponse changeStatut(Long id, VehiculeStatut statut) {
        Vehicule vehicule = getVehiculeEntity(id);
        vehicule.setStatut(statut);
        return VehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    public VehiculeResponse affecterChauffeur(Long vehiculeId, Long chauffeurId) {
        Vehicule vehicule = getVehiculeEntity(vehiculeId);

        if (vehicule.getStatut() == VehiculeStatut.MAINTENANCE
                || vehicule.getStatut() == VehiculeStatut.HORS_SERVICE) {
            throw new VehiculeEnMaintenanceException(
                    vehicule.getId(), vehicule.getImmatriculation(), vehicule.getStatut());
        }

        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new ResourceNotFoundException("Chauffeur", chauffeurId));

        if (chauffeur.getStatut() != ChauffeurStatut.DISPONIBLE) {
            throw new ChauffeurIndisponibleException(chauffeur.getId(), chauffeur.getStatut());
        }
        if (!chauffeur.isPermisValide()) {
            throw new PermisExpireException(
                    chauffeur.getId(), chauffeur.getNumeroPermis(), chauffeur.getDateExpirationPermis());
        }

        vehiculeRepository.findByChauffeurActuelId(chauffeurId)
                .ifPresent(existing -> {
                    existing.setChauffeurActuel(null);
                    vehiculeRepository.save(existing);
                });

        vehicule.setChauffeurActuel(chauffeur);
        return VehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    public VehiculeResponse retirerChauffeur(Long vehiculeId) {
        Vehicule vehicule = getVehiculeEntity(vehiculeId);
        vehicule.setChauffeurActuel(null);
        return VehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    public void deleteVehicule(Long id) {
        Vehicule vehicule = getVehiculeEntity(id);
        if (vehicule.getStatut() == VehiculeStatut.EN_COURSE) {
            throw new ConflictException(
                    "Impossible de supprimer le véhicule " + vehicule.getImmatriculation()
                            + " car il est actuellement en course.");
        }
        vehiculeRepository.delete(vehicule);
    }

    private Vehicule getVehiculeEntity(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(VEHICULE_NOT_FOUND, id));
    }

    private Chauffeur resolveChauffeur(Long chauffeurId) {
        if (chauffeurId == null) {
            return null;
        }
        return chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new ResourceNotFoundException("Chauffeur", chauffeurId));
    }
}
