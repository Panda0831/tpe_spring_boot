package com.tpe.parking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.tpe.parking.entity.Abonnement;
import com.tpe.parking.entity.AbonnementStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    List<Abonnement> findByUtilisateurId(Long utilisateurId);

    List<Abonnement> findByVehiculeId(Long vehiculeId);

    List<Abonnement> findByVehiculeImmatriculation(String immatriculation);

    List<Abonnement> findByStatut(AbonnementStatut statut);

    /** Recherche paginée et filtrée : statut, utilisateur et véhicule sont optionnels. */
    @Query("SELECT a FROM Abonnement a WHERE (:statut IS NULL OR a.statut = :statut) " +
           "AND (:utilisateurId IS NULL OR a.utilisateur.id = :utilisateurId) " +
           "AND (:vehiculeId IS NULL OR a.vehicule.id = :vehiculeId)")
    Page<Abonnement> rechercher(@Param("statut") AbonnementStatut statut,
                                @Param("utilisateurId") Long utilisateurId,
                                @Param("vehiculeId") Long vehiculeId,
                                Pageable pageable);

    @Query("SELECT a FROM Abonnement a WHERE a.vehicule.immatriculation = :immatriculation " +
           "AND a.statut = 'ACTIF' AND :date BETWEEN a.dateDebut AND a.dateFin")
    Optional<Abonnement> findAbonnementActifParImmatriculation(@Param("immatriculation") String immatriculation, @Param("date") LocalDate date);

    @Query("SELECT a FROM Abonnement a WHERE a.vehicule.id = :vehiculeId " +
           "AND a.statut = 'ACTIF' AND :date BETWEEN a.dateDebut AND a.dateFin")
    Optional<Abonnement> findAbonnementActifParVehicule(@Param("vehiculeId") Long vehiculeId, @Param("date") LocalDate date);
}
