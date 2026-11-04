package com.tpe.parking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tpe.parking.entity.EntreeSortie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EntreeSortieRepository extends JpaRepository<EntreeSortie, Long> {

    @Query("SELECT e FROM EntreeSortie e WHERE e.vehicule.immatriculation = :immatriculation AND e.dateSortie IS NULL")
    Optional<EntreeSortie> findEntreeEnCoursParImmatriculation(@Param("immatriculation") String immatriculation);

    @Query("SELECT e FROM EntreeSortie e WHERE e.place.id = :placeId AND e.dateSortie IS NULL")
    Optional<EntreeSortie> findEntreeEnCoursParPlace(@Param("placeId") Long placeId);

    List<EntreeSortie> findByDateEntreeBetween(LocalDateTime debut, LocalDateTime fin);

    List<EntreeSortie> findByDateSortieBetween(LocalDateTime debut, LocalDateTime fin);

    List<EntreeSortie> findByDateSortieIsNull();

    /**
     * Recherche paginée et filtrée de l'historique : sélection des stationnements en cours
     * ({@code enCours = true}) ou terminés ({@code enCours = false}) et recherche par plaque.
     */
    @Query("SELECT e FROM EntreeSortie e WHERE (:enCours IS NULL " +
           "OR (:enCours = TRUE AND e.dateSortie IS NULL) " +
           "OR (:enCours = FALSE AND e.dateSortie IS NOT NULL)) " +
           "AND (:immatriculation = '' OR LOWER(e.vehicule.immatriculation) LIKE LOWER(CONCAT('%', :immatriculation, '%')))")
    Page<EntreeSortie> rechercher(@Param("enCours") Boolean enCours,
                                  @Param("immatriculation") String immatriculation,
                                  Pageable pageable);
}
