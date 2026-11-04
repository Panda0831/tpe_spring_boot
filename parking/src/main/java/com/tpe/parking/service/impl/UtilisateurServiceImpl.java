package com.tpe.parking.service.impl;


import java.util.List;

import com.tpe.parking.dto.request.CreateUtilisateurRequest;
import com.tpe.parking.dto.response.UtilisateurResponse;
import com.tpe.parking.dto.response.VehiculeResponse;
import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.exception.ResourceNotFoundException;
import com.tpe.parking.mapper.UtilisateurMapper;
import com.tpe.parking.mapper.VehiculeMapper;
import com.tpe.parking.repository.UtilisateurRepository;
import com.tpe.parking.repository.VehiculeRepository;
import com.tpe.parking.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
    private final VehiculeRepository vehiculeRepository;

    @Transactional(readOnly = true)
    @Override
    public List<VehiculeResponse> getVehiculesByUtilisateurId(Long utilisateurId) {
        if (!utilisateurRepository.existsById(utilisateurId)) {
            throw new ResourceNotFoundException("Utilisateur introuvable avec l'id : " + utilisateurId);
        }

        return vehiculeRepository.findByUtilisateurId(utilisateurId).stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }
    @Override
    @Transactional
    public UtilisateurResponse creerUtilisateur(CreateUtilisateurRequest request) {
        Utilisateur utilisateur = UtilisateurMapper.toEntity(request);
        return UtilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponse getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .map(UtilisateurMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));
    }
    public List<UtilisateurResponse> getAllUtilisateurs() {
        return utilisateurRepository.findAll()
                .stream()
                .map(UtilisateurMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UtilisateurResponse> rechercherUtilisateurs(String recherche, Pageable pageable) {
        String filtre = recherche == null || recherche.isBlank() ? "" : recherche.trim();
        return utilisateurRepository.rechercher(filtre, pageable)
                .map(UtilisateurMapper::toResponse);
    }
    @Override
    @Transactional

    public UtilisateurResponse modifierUtilisateur(Long id, @NonNull UtilisateurResponse utilisateurResponse) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));

        utilisateur.setNom(utilisateurResponse.nom());
        utilisateur.setPrenom(utilisateurResponse.prenom());
        utilisateur.setEmail(utilisateurResponse.email());

        return UtilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }
    @Override
    @Transactional
    public void supprimerUtilisateur(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id);
        }
        utilisateurRepository.deleteById(id);
    }
}
