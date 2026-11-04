package com.tpe.parking.repository;

import java.util.List;
import java.util.Optional;

import com.tpe.parking.entity.Place;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.PlaceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    /** Recherche paginée et filtrée : zone, type et statut sont tous optionnels. */
    @Query("SELECT p FROM Place p WHERE (:zone = '' OR LOWER(p.zone) = LOWER(:zone)) " +
           "AND (:type IS NULL OR p.type = :type) " +
           "AND (:statut IS NULL OR p.statut = :statut)")
    Page<Place> rechercher(@Param("zone") String zone,
                           @Param("type") PlaceType type,
                           @Param("statut") PlaceStatut statut,
                           Pageable pageable);

    Optional<Place> findByNumero(String numero);

    boolean existsByNumeroIgnoreCase(String numero);

    List<Place> findByStatut(PlaceStatut statut);

    List<Place> findByType(PlaceType type);

    List<Place> findByZone(String zone);

    List<Place> findByStatutAndType(PlaceStatut statut, PlaceType type);

    List<Place> findByStatutAndZone(PlaceStatut statut, String zone);

    long countByStatut(PlaceStatut statut);

    long countByType(PlaceType type);
}
