package com.tpe.parking.mapper;

import com.tpe.parking.dto.UtilisateurDto;
import com.tpe.parking.dto.VehiculeDto;
import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.entity.Vehicule;

public class VehiculeMapper {

    private VehiculeMapper() {
        // Constructeur privé pour empêcher l'instanciation de cette classe utilitaire
    }


    public static VehiculeDto toDto(Vehicule vehicule) {
        if (vehicule == null) {
            return null;
        }

        // Conversion du `long` immatriculation en `String` si le DTO attend une chaîne
        String immatriculationStr = String.valueOf(vehicule.getImmatriculation());

        return new VehiculeDto(
                vehicule.getId(),
                immatriculationStr,
                vehicule.getMarque(),
                vehicule.getModele(),
                vehicule.getUtilisateur() != null ? UtilisateurDto.versDto(vehicule.getUtilisateur()) : null
        );
    }


    public static Vehicule toEntity(VehiculeDto dto, Utilisateur utilisateur) {
        if (dto == null) {
            return null;
        }

        Vehicule vehicule = new Vehicule();

        vehicule.setId(dto.id());
        vehicule.setMarque(dto.marque());
        vehicule.setModele(dto.modele());
        vehicule.setUtilisateur(utilisateur);

        if (dto.immatriculation() != null && !dto.immatriculation().isBlank()) {
            try {
                vehicule.setImmatriculation(Long.parseLong(dto.immatriculation().trim()));
            } catch (NumberFormatException e) {
                vehicule.setImmatriculation(0L);
            }
        }

        return vehicule;
    }
}