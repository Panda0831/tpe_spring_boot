package com.tpe.taxis.service;

import java.util.List;

import com.tpe.taxis.dto.request.CreateClientRequest;
import com.tpe.taxis.dto.request.UpdateClientRequest;
import com.tpe.taxis.dto.response.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    ClientResponse createClient(CreateClientRequest request);

    ClientResponse getClientById(Long id);

    ClientResponse getClientByTelephone(String telephone);

    List<ClientResponse> getAllClients();

    Page<ClientResponse> getAllClients(Pageable pageable);

    Page<ClientResponse> searchClients(String recherche, Pageable pageable);

    ClientResponse updateClient(Long id, UpdateClientRequest request);

    void deleteClient(Long id);
}
