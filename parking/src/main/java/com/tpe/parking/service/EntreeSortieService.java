package com.tpe.parking.service;

import com.tpe.parking.dto.request.EntreeVehiculeRequest;
import com.tpe.parking.dto.request.SortieVehiculeRequest;
import com.tpe.parking.dto.response.EntreeSortieResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Opérations critiques du parking : enregistrement des passages à la borne.
 *
 * <p>Chaque opération est transactionnelle (voir l'implémentation) et réalise la
 * séquence complète exigée par le sujet : recherche des places compatibles →
 * attribution d'une place → changement de statut ({@code LIBRE} ↔ {@code OCCUPEE})
 * → horodatage et facturation.</p>
 */
public interface EntreeSortieService {

    /**
     * Enregistre l'entrée d'un véhicule identifié par sa plaque et attribue automatiquement
     * une place compatible (réservation existante, abonnement, zone souhaitée ou substitution).
     */
    EntreeSortieResponse enregistrerEntree(EntreeVehiculeRequest request);

    /**
     * Enregistre la sortie d'un véhicule, libère sa place et calcule le montant dû
     * (dépassement éventuel inclus).
     */
    EntreeSortieResponse enregistrerSortie(SortieVehiculeRequest request);

    /** Historique complet des entrées/sorties. */
    List<EntreeSortieResponse> getAllEntreesSorties();

    /**
     * Historique paginé, trié et filtrable (stationnements en cours ou terminés, recherche par plaque).
     */
    Page<EntreeSortieResponse> rechercherEntreesSorties(Boolean enCours, String immatriculation, Pageable pageable);

    /** Détail d'une opération d'entrée/sortie. */
    EntreeSortieResponse getEntreeSortieById(Long id);
}
