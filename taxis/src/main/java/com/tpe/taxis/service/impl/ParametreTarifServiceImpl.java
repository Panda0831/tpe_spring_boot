package com.tpe.taxis.service.impl;

import java.util.List;

import com.tpe.taxis.dto.request.ParametreTarifRequest;
import com.tpe.taxis.dto.response.ParametreTarifResponse;
import com.tpe.taxis.entity.ParametreTarif;
import com.tpe.taxis.exception.ResourceNotFoundException;
import com.tpe.taxis.mapper.ParametreTarifMapper;
import com.tpe.taxis.repository.ParametreTarifRepository;
import com.tpe.taxis.service.ParametreTarifService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ParametreTarifServiceImpl implements ParametreTarifService {

    private static final String PARAMETRE_TARIF_NOT_FOUND = "ParametreTarif";

    private final ParametreTarifRepository parametreTarifRepository;

    @Override
    public ParametreTarifResponse createParametreTarif(ParametreTarifRequest request) {
        ParametreTarif parametre = ParametreTarifMapper.toEntity(request);
        return ParametreTarifMapper.toResponse(parametreTarifRepository.save(parametre));
    }

    @Override
    @Transactional(readOnly = true)
    public ParametreTarifResponse getParametreTarifById(Long id) {
        return ParametreTarifMapper.toResponse(getParametreEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParametreTarifResponse> getAllParametresTarif() {
        return parametreTarifRepository.findAll()
                .stream()
                .map(ParametreTarifMapper::toResponse)
                .toList();
    }

    @Override
    public ParametreTarifResponse updateParametreTarif(Long id, ParametreTarifRequest request) {
        ParametreTarif parametre = getParametreEntity(id);
        ParametreTarifMapper.updateEntity(parametre, request);
        return ParametreTarifMapper.toResponse(parametreTarifRepository.save(parametre));
    }

    @Override
    public void deleteParametreTarif(Long id) {
        ParametreTarif parametre = getParametreEntity(id);
        parametreTarifRepository.delete(parametre);
    }

    private ParametreTarif getParametreEntity(Long id) {
        return parametreTarifRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PARAMETRE_TARIF_NOT_FOUND, id));
    }
}
