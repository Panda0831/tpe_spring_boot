package com.tpe.taxis.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.tpe.taxis.entity.Course;
import com.tpe.taxis.entity.CourseStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByStatut(CourseStatut statut);

    Page<Course> findByStatut(CourseStatut statut, Pageable pageable);

    Page<Course> findByClientId(Long clientId, Pageable pageable);

    Page<Course> findByChauffeurId(Long chauffeurId, Pageable pageable);

    /**
     * Courses terminées d'un chauffeur, les plus récentes d'abord (critère de proximité du dispatcher).
     */
    List<Course> findByChauffeurIdAndStatutOrderByDateHeureDesc(Long chauffeurId, CourseStatut statut);

    Page<Course> findByVehiculeId(Long vehiculeId, Pageable pageable);

    List<Course> findByDateHeureBetween(LocalDateTime debut, LocalDateTime fin);

    Page<Course> findByDateHeureBetween(LocalDateTime debut, LocalDateTime fin, Pageable pageable);

    boolean existsByChauffeurIdAndStatutIn(Long chauffeurId, List<CourseStatut> statuts);

    /**
     * Nombre de courses affectées à un chauffeur depuis une date (fenêtre glissante),
     * utilisé pour le critère d'équité du score d'attribution automatique.
     */
    long countByChauffeurIdAndDateHeureAfter(Long chauffeurId, LocalDateTime depuis);

    /**
     * Courses actives d'un chauffeur (anti double-affectation / anti-chevauchement).
     */
    @Query("SELECT c FROM Course c WHERE c.chauffeur.id = :chauffeurId " +
           "AND c.statut IN ('ASSIGNEE', 'EN_COURS')")
    List<Course> findCoursesActivesByChauffeur(@Param("chauffeurId") Long chauffeurId);

    /**
     * Chiffre d'affaires réalisé (courses terminées) sur une période.
     */
    @Query("SELECT COALESCE(SUM(c.tarif), 0) FROM Course c " +
           "WHERE c.statut = 'TERMINEE' AND c.dateHeure BETWEEN :debut AND :fin")
    BigDecimal sumCaByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    /**
     * Total des commissions de la société sur une période.
     */
    @Query("SELECT COALESCE(SUM(c.commission), 0) FROM Course c " +
           "WHERE c.statut = 'TERMINEE' AND c.dateHeure BETWEEN :debut AND :fin")
    BigDecimal sumCommissionByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    /**
     * Total reversé aux chauffeurs sur une période.
     */
    @Query("SELECT COALESCE(SUM(c.montantChauffeur), 0) FROM Course c " +
           "WHERE c.statut = 'TERMINEE' AND c.dateHeure BETWEEN :debut AND :fin")
    BigDecimal sumMontantChauffeurByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    /**
     * CA agrégé par chauffeur sur une période (classement).
     */
    @Query("SELECT c.chauffeur.id, c.chauffeur.nom, c.chauffeur.prenom, COALESCE(SUM(c.tarif), 0), COUNT(c) " +
           "FROM Course c WHERE c.statut = 'TERMINEE' " +
           "AND c.dateHeure BETWEEN :debut AND :fin " +
           "GROUP BY c.chauffeur.id, c.chauffeur.nom, c.chauffeur.prenom " +
           "ORDER BY COALESCE(SUM(c.tarif), 0) DESC")
    List<Object[]> sumCaParChauffeur(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    /**
     * CA agrégé par véhicule sur une période.
     */
    @Query("SELECT c.vehicule.id, c.vehicule.immatriculation, COALESCE(SUM(c.tarif), 0), COUNT(c) " +
           "FROM Course c WHERE c.statut = 'TERMINEE' " +
           "AND c.dateHeure BETWEEN :debut AND :fin " +
           "GROUP BY c.vehicule.id, c.vehicule.immatriculation " +
           "ORDER BY COALESCE(SUM(c.tarif), 0) DESC")
    List<Object[]> sumCaParVehicule(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    /**
     * Distance totale parcourue (courses terminées) sur une période.
     */
    @Query("SELECT COALESCE(SUM(c.distanceKm), 0) FROM Course c " +
           "WHERE c.statut = 'TERMINEE' AND c.dateHeure BETWEEN :debut AND :fin")
    Double sumDistanceByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    /**
     * Nombre de courses par statut sur une période (taux d'annulation, etc.).
     */
    @Query("SELECT c.statut, COUNT(c) FROM Course c " +
           "WHERE c.dateHeure BETWEEN :debut AND :fin " +
           "GROUP BY c.statut")
    List<Object[]> countByStatutBetween(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}
