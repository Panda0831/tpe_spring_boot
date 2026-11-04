package com.tpe.taxis.service.impl;

import java.util.List;

import com.tpe.taxis.dto.request.CreateClientRequest;
import com.tpe.taxis.dto.request.UpdateClientRequest;
import com.tpe.taxis.dto.response.ClientResponse;
import com.tpe.taxis.entity.Client;
import com.tpe.taxis.exception.ConflictException;
import com.tpe.taxis.exception.ResourceNotFoundException;
import com.tpe.taxis.mapper.ClientMapper;
import com.tpe.taxis.repository.ClientRepository;
import com.tpe.taxis.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private static final String CLIENT_NOT_FOUND = "Client";

    private final ClientRepository clientRepository;

    @Override
    public ClientResponse createClient(CreateClientRequest request) {
        String telephone = request.telephone().trim();
        if (clientRepository.existsByTelephone(telephone)) {
            throw new ConflictException("Un client avec le téléphone " + telephone + " existe déjà.");
        }

        Client client = ClientMapper.toEntity(request);
        return ClientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        return ClientMapper.toResponse(getClientEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientByTelephone(String telephone) {
        String tel = telephone != null ? telephone.trim() : null;
        return clientRepository.findByTelephone(tel)
                .map(ClientMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "téléphone", telephone));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(ClientMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable)
                .map(ClientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> searchClients(String recherche, Pageable pageable) {
        String q = recherche == null ? "" : recherche.trim();
        return clientRepository.rechercher(q, pageable)
                .map(ClientMapper::toResponse);
    }

    @Override
    public ClientResponse updateClient(Long id, UpdateClientRequest request) {
        Client client = getClientEntity(id);

        if (request.telephone() != null && !request.telephone().isBlank()) {
            String telephone = request.telephone().trim();
            if (!telephone.equals(client.getTelephone())
                    && clientRepository.existsByTelephone(telephone)) {
                throw new ConflictException("Un client avec le téléphone " + telephone + " existe déjà.");
            }
        }

        ClientMapper.updateEntity(client, request);
        return ClientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    public void deleteClient(Long id) {
        Client client = getClientEntity(id);
        if (!client.getCourses().isEmpty()) {
            throw new ConflictException(
                    "Impossible de supprimer le client " + client.getNom()
                            + " car il possède un historique de courses.");
        }
        clientRepository.delete(client);
    }

    private Client getClientEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CLIENT_NOT_FOUND, id));
    }
}
