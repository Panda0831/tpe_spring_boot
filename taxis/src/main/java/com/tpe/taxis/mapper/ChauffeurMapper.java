package com.tpe.taxis.mapper;

import com.tpe.taxis.dto.request.CreateChauffeurRequest;
import com.tpe.taxis.dto.request.UpdateChauffeurRequest;
import com.tpe.taxis.dto.response.ChauffeurResponse;
import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.ChauffeurStatut;

import java.time.LocalDate;

public final class ChauffeurMapper {

    private ChauffeurMapper() {
    }

    public static ChauffeurResponse toResponse(Chauffeur chauffeur) {
        if (chauffeur == null) {
            return null;
        }
        return ChauffeurResponse.fromEntity(chauffeur);
    }

    public static Chauffeur toEntity(CreateChauffeurRequest request) {
        if (request == null) {
            return null;
        }
        return Chauffeur.builder()
                .nom(request.nom().trim())
                .prenom(request.prenom().trim())
                .telephone(request.telephone().trim())
                .numeroPermis(request.numeroPermis().trim().toUpperCase())
                .dateExpirationPermis(request.dateExpirationPermis())
                .dateEmbauche(request.dateEmbauche() != null ? request.dateEmbauche() : LocalDate.now())
                .statut(request.statut() != null ? request.statut() : ChauffeurStatut.DISPONIBLE)
                .build();
    }

    public static void updateEntity(Chauffeur chauffeur, UpdateChauffeurRequest request) {
        if (chauffeur == null || request == null) {
            return;
        }
        if (request.nom() != null && !request.nom().isBlank()) {
            chauffeur.setNom(request.nom().trim());
        }
        if (request.prenom() != null && !request.prenom().isBlank()) {
            chauffeur.setPrenom(request.prenom().trim());
        }
        if (request.telephone() != null && !request.telephone().isBlank()) {
            chauffeur.setTelephone(request.telephone().trim());
        }
        if (request.numeroPermis() != null && !request.numeroPermis().isBlank()) {
            chauffeur.setNumeroPermis(request.numeroPermis().trim().toUpperCase());
        }
        if (request.dateExpirationPermis() != null) {
            chauffeur.setDateExpirationPermis(request.dateExpirationPermis());
        }
        if (request.statut() != null) {
            chauffeur.setStatut(request.statut());
        }
        if (request.dateEmbauche() != null) {
            chauffeur.setDateEmbauche(request.dateEmbauche());
        }
    }
}
