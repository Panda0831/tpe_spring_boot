package com.tpe.taxis.mapper;

import com.tpe.taxis.dto.request.CreateClientRequest;
import com.tpe.taxis.dto.request.UpdateClientRequest;
import com.tpe.taxis.dto.response.ClientResponse;
import com.tpe.taxis.entity.Client;

public final class ClientMapper {

    private ClientMapper() {
    }

    public static ClientResponse toResponse(Client client) {
        if (client == null) {
            return null;
        }
        return ClientResponse.fromEntity(client);
    }

    public static Client toEntity(CreateClientRequest request) {
        if (request == null) {
            return null;
        }
        return Client.builder()
                .nom(request.nom().trim())
                .prenom(request.prenom() != null && !request.prenom().isBlank() ? request.prenom().trim() : null)
                .telephone(request.telephone().trim())
                .email(request.email() != null && !request.email().isBlank() ? request.email().trim().toLowerCase() : null)
                .build();
    }

    public static void updateEntity(Client client, UpdateClientRequest request) {
        if (client == null || request == null) {
            return;
        }
        if (request.nom() != null && !request.nom().isBlank()) {
            client.setNom(request.nom().trim());
        }
        if (request.prenom() != null) {
            client.setPrenom(request.prenom().isBlank() ? null : request.prenom().trim());
        }
        if (request.telephone() != null && !request.telephone().isBlank()) {
            client.setTelephone(request.telephone().trim());
        }
        if (request.email() != null) {
            client.setEmail(request.email().isBlank() ? null : request.email().trim().toLowerCase());
        }
    }
}
