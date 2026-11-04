package com.tpe.parking.repository;

import java.util.List;
import java.util.Optional;

import com.tpe.parking.entity.Vehicule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    List<Vehicule> findByUtilisateurId(Long utilisateurId);

    /** Recherche paginée sur la plaque, la marque ou le modèle, avec filtre propriétaire. */
    @Query("SELECT v FROM Vehicule v WHERE (:utilisateurId IS NULL OR v.utilisateur.id = :utilisateurId) " +
           "AND (:recherche = '' " +
           "OR LOWER(v.immatriculation) LIKE LOWER(CONCAT('%', :recherche, '%')) " +
           "OR LOWER(v.marque) LIKE LOWER(CONCAT('%', :recherche, '%')) " +
           "OR LOWER(v.modele) LIKE LOWER(CONCAT('%', :recherche, '%')))")
    Page<Vehicule> rechercher(@Param("recherche") String recherche,
                              @Param("utilisateurId") Long utilisateurId,
                              Pageable pageable);
}
