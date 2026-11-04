package com.tpe.parking.service;

import com.tpe.parking.dto.request.CreateTarifRequest;
import com.tpe.parking.dto.request.UpdateTarifRequest;
import com.tpe.parking.dto.response.TarifResponse;
import com.tpe.parking.entity.Role;
import com.tpe.parking.entity.Tarif;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TarifService {
    long getNombreTotalTarifs();

    List<Tarif> getAllTarifs();

    Tarif getTarifApplicable(String zone);

    // --- Configuration des tarifs (réservée au rôle ADMIN côté sécurité) ---

    TarifResponse getTarifById(Long id);

    TarifResponse creerTarif(CreateTarifRequest request);

    TarifResponse modifierTarif(Long id, UpdateTarifRequest request);

    void supprimerTarif(Long id);

    BigDecimal calculerMontant(LocalDateTime dateEntree, LocalDateTime dateSortie, String zone,
                              boolean abonne, Role role, String typeVehicule);

    BigDecimal calculerMontantAvecDepassement(LocalDateTime dateEntree, LocalDateTime dateSortie,
                                             LocalDateTime dateDepartPrevue, String zone,
                                             boolean abonne, Role role, String typeVehicule);
}
