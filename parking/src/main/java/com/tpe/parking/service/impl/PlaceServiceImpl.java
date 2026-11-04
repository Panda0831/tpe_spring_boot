package com.tpe.parking.service.impl;

import com.tpe.parking.dto.request.CreatePlaceRequest;
import com.tpe.parking.dto.response.PlaceResponse;
import com.tpe.parking.entity.Place;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.PlaceType;
import com.tpe.parking.exception.ConflictException;
import com.tpe.parking.exception.InvalidOperationException;
import com.tpe.parking.exception.ResourceNotFoundException;
import com.tpe.parking.repository.PlaceRepository;
import com.tpe.parking.repository.UtilisateurRepository;
import com.tpe.parking.repository.VehiculeRepository;
import com.tpe.parking.service.PlaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlaceServiceImpl implements PlaceService {
    private final UtilisateurRepository utilisateurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PlaceRepository placeRepository;

    public PlaceServiceImpl(UtilisateurRepository utilisateurRepository,
                            VehiculeRepository vehiculeRepository,
                            PlaceRepository placeRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.placeRepository = placeRepository;
    }

    public void assignerPlaceParking(Long utilisateurId, Long vehiculeId) {
        var utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + utilisateurId));

        var vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + vehiculeId));

        if (vehicule.getUtilisateur() != null && !vehicule.getUtilisateur().getId().equals(utilisateurId)) {
            throw new ConflictException("Le véhicule appartient déjà à un autre utilisateur");
        }

        vehicule.setUtilisateur(utilisateur);
        vehiculeRepository.save(vehicule);
    }

    public void libererPlaceParking(Long utilisateurId, Long vehiculeId) {
        utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + utilisateurId));

        var vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + vehiculeId));

        if (vehicule.getUtilisateur() == null || !vehicule.getUtilisateur().getId().equals(utilisateurId)) {
            throw new InvalidOperationException(
                    "Le véhicule n'appartient pas à l'utilisateur avec l'id : " + utilisateurId);
        }

        vehicule.setUtilisateur(null);
        vehiculeRepository.save(vehicule);
    }

    public void transfererPlaceParking(Long utilisateurSourceId, Long utilisateurDestinationId, Long vehiculeId) {
        var utilisateurSource = utilisateurRepository.findById(utilisateurSourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur source non trouvé avec l'id : " + utilisateurSourceId));

        var utilisateurDestination = utilisateurRepository.findById(utilisateurDestinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur destination non trouvé avec l'id : " + utilisateurDestinationId));

        var vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + vehiculeId));

        if (vehicule.getUtilisateur() == null || !vehicule.getUtilisateur().getId().equals(utilisateurSourceId)) {
            throw new InvalidOperationException(
                    "Le véhicule n'appartient pas à l'utilisateur source avec l'id : " + utilisateurSourceId);
        }

        vehicule.setUtilisateur(utilisateurDestination);
        vehiculeRepository.save(vehicule);
    }
    public List<PlaceResponse> getAllPlaces() {
        return placeRepository.findAll()
                .stream()
                .map(PlaceResponse::fromEntity)
                .toList();
    }

    @Override
    public PlaceResponse creerPlace(CreatePlaceRequest request) {
        String numero = request.numero().trim().toUpperCase();
        String zone = request.zone().trim();
        if (placeRepository.existsByNumeroIgnoreCase(numero)) {
            throw new ConflictException("Une place avec le numéro " + numero + " existe déjà.");
        }

        Place place = new Place(null, numero, zone, request.type(), PlaceStatut.LIBRE);
        try {
            return PlaceResponse.fromEntity(placeRepository.save(place));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Une place avec le numéro " + numero + " existe déjà.");
        }
    }

    @Override
    public Page<PlaceResponse> rechercherPlaces(String zone, PlaceType type, PlaceStatut statut, Pageable pageable) {
        return placeRepository.rechercher(normaliser(zone), type, statut, pageable)
                .map(PlaceResponse::fromEntity);
    }

    private String normaliser(String valeur) {
        return valeur == null || valeur.isBlank() ? "" : valeur.trim();
    }

}
