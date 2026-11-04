package com.tpe.taxis.dto.response;

import com.tpe.taxis.entity.Client;

public record ClientResponse(
        Long id,
        String nom,
        String prenom,
        String nomComplet,
        String telephone,
        String email,
        int totalCourses
) {
    public static ClientResponse fromEntity(Client client) {
        if (client == null) {
            return null;
        }

        String nomComplet = ((client.getPrenom() != null ? client.getPrenom() : "") + " " +
                (client.getNom() != null ? client.getNom() : "")).trim();

        int coursesCount = (client.getCourses() != null) ? client.getCourses().size() : 0;

        return new ClientResponse(
                client.getId(),
                client.getNom(),
                client.getPrenom(),
                nomComplet,
                client.getTelephone(),
                client.getEmail(),
                coursesCount
        );
    }
}
