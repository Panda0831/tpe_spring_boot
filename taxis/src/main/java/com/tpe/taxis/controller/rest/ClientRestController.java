package com.tpe.taxis.controller.rest;

import com.tpe.taxis.dto.request.CreateClientRequest;
import com.tpe.taxis.dto.request.UpdateClientRequest;
import com.tpe.taxis.dto.response.ClientResponse;
import com.tpe.taxis.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientRestController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody CreateClientRequest request) {
        ClientResponse response = clientService.createClient(request);
        return ResponseEntity.created(URI.create("/api/clients/" + response.id())).body(response);
    }

    @GetMapping
    public Page<ClientResponse> getAllClients(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return clientService.getAllClients(pageable);
    }

    @GetMapping("/all")
    public List<ClientResponse> getAllClientsList() {
        return clientService.getAllClients();
    }

    @GetMapping("/{id}")
    public ClientResponse getClientById(@PathVariable Long id) {
        return clientService.getClientById(id);
    }

    @GetMapping("/telephone/{telephone}")
    public ClientResponse getClientByTelephone(@PathVariable String telephone) {
        return clientService.getClientByTelephone(telephone);
    }

    @GetMapping("/search")
    public Page<ClientResponse> searchClients(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return clientService.searchClients(q, pageable);
    }

    @PutMapping("/{id}")
    public ClientResponse updateClient(@PathVariable Long id,
                                       @Valid @RequestBody UpdateClientRequest request) {
        return clientService.updateClient(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
