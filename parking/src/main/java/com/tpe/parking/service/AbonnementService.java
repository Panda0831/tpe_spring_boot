package com.tpe.parking.service;

import java.time.LocalDate;
import java.util.List;

import com.tpe.parking.dto.request.CreateAbonnementRequest;
import com.tpe.parking.dto.response.AbonnementResponse;
import com.tpe.parking.entity.AbonnementStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AbonnementService {

    List<AbonnementResponse> getAllAbonnements();

    /** Liste paginée, triée et filtrée des abonnements : statut, utilisateur et véhicule optionnels. */
    Page<AbonnementResponse> rechercherAbonnements(AbonnementStatut statut,
                                                   Long utilisateurId,
                                                   Long vehiculeId,
                                                   Pageable pageable);

    AbonnementResponse getAbonnementById(Long id);

    List<AbonnementResponse> getAbonnementsByUtilisateur(Long utilisateurId);

    List<AbonnementResponse> getAbonnementsByVehicule(Long vehiculeId);

    List<AbonnementResponse> getAbonnementsByStatut(AbonnementStatut statut);

    AbonnementResponse souscrireAbonnement(CreateAbonnementRequest request);

    AbonnementResponse renouvelerAbonnement(Long id, LocalDate nouvelleDateFin);

    void resilierAbonnement(Long id);

    boolean estAbonneActif(String immatriculation);

    boolean estAbonneActif(Long vehiculeId);

    boolean hasActiveAbonnement(Long utilisateurId);
}
