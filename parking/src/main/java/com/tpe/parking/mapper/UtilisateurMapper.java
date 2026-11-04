package com.tpe.parking.mapper;

import com.tpe.parking.dto.request.CreateUtilisateurRequest;
import com.tpe.parking.dto.response.UtilisateurResponse;
import com.tpe.parking.entity.Role;
import com.tpe.parking.entity.Utilisateur;

public final class UtilisateurMapper {

    private UtilisateurMapper() {
    }

    public static UtilisateurResponse toResponse(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }
        return UtilisateurResponse.fromEntity(utilisateur);
    }

    public static Utilisateur toEntity(CreateUtilisateurRequest request) {
        if (request == null) {
            return null;
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.nom());
        utilisateur.setPrenom(request.prenom());
        utilisateur.setTelephone(request.telephone());
        utilisateur.setEmail(request.email());
        utilisateur.setMotDePasse(request.motDePasse());
        utilisateur.setRole(request.role() != null ? request.role() : Role.USER);
        return utilisateur;
    }
}
