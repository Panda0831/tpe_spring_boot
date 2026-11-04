package com.tpe.parking.mapper;

import com.tpe.parking.dto.request.CreateVehiculeRequest;
import com.tpe.parking.dto.response.VehiculeResponse;
import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.entity.Vehicule;

public final class VehiculeMapper {

    private VehiculeMapper() {
    }

    public static VehiculeResponse toResponse(Vehicule vehicule) {
        if (vehicule == null) {
            return null;
        }
        return VehiculeResponse.fromEntity(vehicule);
    }

    public static Vehicule toEntity(CreateVehiculeRequest request, Utilisateur proprietaire) {
        if (request == null) {
            return null;
        }
        Vehicule vehicule = new Vehicule();
        vehicule.setImmatriculation(request.immatriculation().trim().toUpperCase());
        vehicule.setMarque(request.marque());
        vehicule.setModele(request.modele());
        vehicule.setUtilisateur(proprietaire);
        return vehicule;
    }
}