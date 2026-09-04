package com.tpe.parking.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.tpe.parking.dto.UtilisateurDto;
import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.entity.Vehicule;

public class UtilisateurMapper {
    private UtilisateurMapper() {
    }

    public static UtilisateurDto toDto(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        return new UtilisateurDto(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getTelephone(),
                utilisateur.getVehicules() == null ? List.of()
                        : utilisateur.getVehicules().stream()
                                .filter(Objects::nonNull)
                                .map(v -> String.valueOf(v.getImmatriculation()))
                                .toList()
        );
    }

    public static Utilisateur toEntity(UtilisateurDto dto) {
        if (dto == null) {
            return null;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(dto.getId());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setTelephone(dto.getTelephone());

        if (dto.getVehicules() != null) {
            List<Vehicule> vehicules = new ArrayList<>();
            for (String immatriculation : dto.getVehicules()) {
                if (immatriculation == null || immatriculation.isBlank()) {
                    continue;
                }
                Vehicule vehicule = new Vehicule();
                vehicule.setImmatriculation(Long.parseLong(immatriculation.trim()));
                vehicule.setUtilisateur(utilisateur);
                vehicules.add(vehicule);
            }
            utilisateur.setVehicules(vehicules);
        }

        return utilisateur;
    }
}
