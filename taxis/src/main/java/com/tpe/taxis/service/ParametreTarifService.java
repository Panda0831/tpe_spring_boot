package com.tpe.taxis.service;

import java.util.List;

import com.tpe.taxis.dto.request.ParametreTarifRequest;
import com.tpe.taxis.dto.response.ParametreTarifResponse;

public interface ParametreTarifService {

    ParametreTarifResponse createParametreTarif(ParametreTarifRequest request);

    ParametreTarifResponse getParametreTarifById(Long id);

    List<ParametreTarifResponse> getAllParametresTarif();

    ParametreTarifResponse updateParametreTarif(Long id, ParametreTarifRequest request);

    void deleteParametreTarif(Long id);
}
