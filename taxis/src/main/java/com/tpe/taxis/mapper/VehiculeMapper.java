package com.tpe.taxis.mapper;

import com.tpe.taxis.dto.request.CreateVehiculeRequest;
import com.tpe.taxis.dto.request.UpdateVehiculeRequest;
import com.tpe.taxis.dto.response.VehiculeResponse;
import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.Vehicule;
import com.tpe.taxis.entity.VehiculeStatut;

public final class VehiculeMapper {

    private VehiculeMapper() {
    }

    public static VehiculeResponse toResponse(Vehicule vehicule) {
        if (vehicule == null) {
            return null;
        }
        return VehiculeResponse.fromEntity(vehicule);
    }

    public static Vehicule toEntity(CreateVehiculeRequest request, Chauffeur chauffeur) {
        if (request == null) {
            return null;
        }
        return Vehicule.builder()
                .immatriculation(request.immatriculation().trim().toUpperCase())
                .marque(request.marque().trim())
                .modele(request.modele().trim())
                .annee(request.annee())
                .capacite(request.capacite())
                .kilometrage(request.kilometrage() != null ? request.kilometrage() : 0.0)
                .statut(request.statut() != null ? request.statut() : VehiculeStatut.DISPONIBLE)
                .chauffeurActuel(chauffeur)
                .build();
    }

    public static Vehicule toEntity(CreateVehiculeRequest request) {
        return toEntity(request, null);
    }

    public static void updateEntity(Vehicule vehicule, UpdateVehiculeRequest request, Chauffeur chauffeur) {
        if (vehicule == null || request == null) {
            return;
        }
        if (request.immatriculation() != null && !request.immatriculation().isBlank()) {
            vehicule.setImmatriculation(request.immatriculation().trim().toUpperCase());
        }
        if (request.marque() != null && !request.marque().isBlank()) {
            vehicule.setMarque(request.marque().trim());
        }
        if (request.modele() != null && !request.modele().isBlank()) {
            vehicule.setModele(request.modele().trim());
        }
        if (request.annee() != null) {
            vehicule.setAnnee(request.annee());
        }
        if (request.capacite() != null) {
            vehicule.setCapacite(request.capacite());
        }
        if (request.kilometrage() != null) {
            vehicule.setKilometrage(request.kilometrage());
        }
        if (request.statut() != null) {
            vehicule.setStatut(request.statut());
        }
        if (request.chauffeurId() != null) {
            vehicule.setChauffeurActuel(chauffeur);
        }
    }

    public static void updateEntity(Vehicule vehicule, UpdateVehiculeRequest request) {
        updateEntity(vehicule, request, null);
    }
}
