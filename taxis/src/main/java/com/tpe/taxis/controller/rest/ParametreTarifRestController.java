package com.tpe.taxis.controller.rest;

import com.tpe.taxis.dto.request.ParametreTarifRequest;
import com.tpe.taxis.dto.response.ParametreTarifResponse;
import com.tpe.taxis.service.ParametreTarifService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tarifs")
@RequiredArgsConstructor
public class ParametreTarifRestController {

    private final ParametreTarifService parametreTarifService;

    @PostMapping
    public ResponseEntity<ParametreTarifResponse> createParametreTarif(
            @Valid @RequestBody ParametreTarifRequest request) {
        ParametreTarifResponse response = parametreTarifService.createParametreTarif(request);
        return ResponseEntity.created(URI.create("/api/tarifs/" + response.id())).body(response);
    }

    @GetMapping
    public List<ParametreTarifResponse> getAllParametresTarif() {
        return parametreTarifService.getAllParametresTarif();
    }

    @GetMapping("/{id}")
    public ParametreTarifResponse getParametreTarifById(@PathVariable Long id) {
        return parametreTarifService.getParametreTarifById(id);
    }

    @PutMapping("/{id}")
    public ParametreTarifResponse updateParametreTarif(@PathVariable Long id,
                                                       @Valid @RequestBody ParametreTarifRequest request) {
        return parametreTarifService.updateParametreTarif(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParametreTarif(@PathVariable Long id) {
        parametreTarifService.deleteParametreTarif(id);
        return ResponseEntity.noContent().build();
    }
}
