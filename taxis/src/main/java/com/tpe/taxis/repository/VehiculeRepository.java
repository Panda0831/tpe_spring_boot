package com.tpe.taxis.repository;

import java.util.List;
import java.util.Optional;

import com.tpe.taxis.entity.Vehicule;
import com.tpe.taxis.entity.VehiculeStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    List<Vehicule> findByStatut(VehiculeStatut statut);

    Page<Vehicule> findByStatut(VehiculeStatut statut, Pageable pageable);

    Page<Vehicule> findByMarqueContainingIgnoreCaseOrModeleContainingIgnoreCase(
            String marque, String modele, Pageable pageable);

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    Optional<Vehicule> findByChauffeurActuelId(Long chauffeurId);

    boolean existsByImmatriculation(String immatriculation);

    boolean existsByChauffeurActuelId(Long chauffeurId);

    long countByStatut(VehiculeStatut statut);

    /**
     * Véhicules disponibles et hors maintenance, éligibles pour une course.
     */
    @Query("SELECT v FROM Vehicule v WHERE v.statut = 'DISPONIBLE'")
    List<Vehicule> findDisponiblesPourCourse();

    /**
     * Somme des kilométrages de la flotte (statistiques).
     */
    @Query("SELECT COALESCE(SUM(v.kilometrage), 0) FROM Vehicule v")
    Double sumKilometrage();
}
