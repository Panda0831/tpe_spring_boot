package com.tpe.parking.service.impl;

import java.util.List;

import com.tpe.parking.dto.request.CreateVehiculeRequest;
import com.tpe.parking.dto.response.VehiculeResponse;
import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.entity.Vehicule;
import com.tpe.parking.exception.ConflictException;
import com.tpe.parking.exception.ResourceNotFoundException;
import com.tpe.parking.mapper.VehiculeMapper;
import com.tpe.parking.repository.UtilisateurRepository;
import com.tpe.parking.repository.VehiculeRepository;
import com.tpe.parking.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VehiculeServiceImpl implements VehiculeService {
    private final VehiculeRepository vehiculeRepository;
    private final UtilisateurRepository utilisateurRepository;

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
    public List<VehiculeResponse> getVehiculesByUtilisateurId(Long utilisateurId) {
        return vehiculeRepository.findByUtilisateurId(utilisateurId)
                .stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehiculeResponse> rechercherVehicules(String recherche, Long utilisateurId, Pageable pageable) {
        String filtre = recherche == null || recherche.isBlank() ? "" : recherche.trim();
        return vehiculeRepository.rechercher(filtre, utilisateurId, pageable)
                .map(VehiculeMapper::toResponse);
    }

    @Override
    public VehiculeResponse createVehicule(CreateVehiculeRequest request) {
        Utilisateur proprietaire = utilisateurRepository.findById(request.utilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + request.utilisateurId()));

        String immatriculation = request.immatriculation() != null ? request.immatriculation().trim().toUpperCase() : null;
        if (vehiculeRepository.findByImmatriculation(immatriculation).isPresent()) {
            throw new ConflictException("Un véhicule avec l'immatriculation " + immatriculation + " existe déjà.");
        }

        Vehicule vehicule = VehiculeMapper.toEntity(request, proprietaire);
        return VehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    public VehiculeResponse updateVehicule(Long id, CreateVehiculeRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule non trouvé avec l'id : " + id));

        Utilisateur proprietaire = utilisateurRepository.findById(request.utilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + request.utilisateurId()));

        String immatriculation = request.immatriculation() != null ? request.immatriculation().trim().toUpperCase() : null;
        if (immatriculation != null && !immatriculation.equals(vehicule.getImmatriculation())
                && vehiculeRepository.findByImmatriculation(immatriculation).isPresent()) {
            throw new ConflictException("Un véhicule avec l'immatriculation " + immatriculation + " existe déjà.");
        }

        vehicule.setImmatriculation(immatriculation);
        vehicule.setMarque(request.marque());
        vehicule.setModele(request.modele());
        vehicule.setUtilisateur(proprietaire);

        return VehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeResponse getVehiculeById(Long id) {
        return vehiculeRepository.findById(id)
                .map(VehiculeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule non trouvé avec l'id : " + id));
    }

    @Override
    public void deleteVehicule(Long id) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule non trouvé avec l'id : " + id));
        vehiculeRepository.delete(vehicule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> getVehiculesByImmatriculation(String immatriculation) {
        if (immatriculation == null || immatriculation.isBlank()) {
            return List.of();
        }
        String filtre = immatriculation.trim().toUpperCase();
        return vehiculeRepository.findAll().stream()
                .filter(v -> v.getImmatriculation() != null
                        && v.getImmatriculation().toUpperCase().contains(filtre))
                .map(VehiculeMapper::toResponse)
                .toList();
    }
}
