package com.tpe.taxis.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.ChauffeurStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChauffeurRepository extends JpaRepository<Chauffeur, Long> {

    List<Chauffeur> findByStatut(ChauffeurStatut statut);

    Page<Chauffeur> findByStatut(ChauffeurStatut statut, Pageable pageable);

    Page<Chauffeur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
            String nom, String prenom, Pageable pageable);

    Optional<Chauffeur> findByNumeroPermis(String numeroPermis);

    boolean existsByNumeroPermis(String numeroPermis);

    long countByStatut(ChauffeurStatut statut);

    /**
     * Chauffeurs éligibles pour une course : statut DISPONIBLE et permis encore valide à la date donnée.
     * (La disponibilité du véhicule associé est vérifiée en amont par le dispatcher.)
     */
    @Query("SELECT ch FROM Chauffeur ch WHERE ch.statut = 'DISPONIBLE' " +
           "AND ch.dateExpirationPermis >= :date")
    List<Chauffeur> findEligiblesPourCourse(@Param("date") LocalDate date);
}
