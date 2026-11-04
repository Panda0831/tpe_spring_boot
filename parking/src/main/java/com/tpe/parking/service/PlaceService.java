package com.tpe.parking.service;

import com.tpe.parking.dto.request.CreatePlaceRequest;
import com.tpe.parking.dto.response.PlaceResponse;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.PlaceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlaceService {
    void assignerPlaceParking(Long utilisateurId, Long vehiculeId);

    void libererPlaceParking(Long utilisateurId, Long vehiculeId);

    void transfererPlaceParking(Long utilisateurSourceId, Long utilisateurDestinationId, Long vehiculeId);

    List<PlaceResponse> getAllPlaces();

    PlaceResponse creerPlace(CreatePlaceRequest request);

    /** Liste paginée et filtrée des places (zone, type et statut optionnels). */
    Page<PlaceResponse> rechercherPlaces(String zone, PlaceType type, PlaceStatut statut, Pageable pageable);
}
