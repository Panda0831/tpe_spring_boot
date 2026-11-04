package com.tpe.parking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tpe.parking.entity.Reservation;
import com.tpe.parking.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface
ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUtilisateurId(Long utilisateurId);

    List<Reservation> findByVehiculeId(Long vehiculeId);

    List<Reservation> findByPlaceId(Long placeId);

    List<Reservation> findByStatut(ReservationStatus statut);

    /**
     * Recherche paginée et filtrée : statut, utilisateur, véhicule et place sont optionnels.
     */
    @Query("SELECT r FROM Reservation r WHERE (:statut IS NULL OR r.statut = :statut) " +
           "AND (:utilisateurId IS NULL OR r.utilisateur.id = :utilisateurId) " +
           "AND (:vehiculeId IS NULL OR r.vehicule.id = :vehiculeId) " +
           "AND (:placeId IS NULL OR r.place.id = :placeId)")
    Page<Reservation> rechercher(@Param("statut") ReservationStatus statut,
                                 @Param("utilisateurId") Long utilisateurId,
                                 @Param("vehiculeId") Long vehiculeId,
                                 @Param("placeId") Long placeId,
                                 Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.place.id = :placeId " +
           "AND r.statut NOT IN ('ANNULEE', 'TERMINEE') " +
           "AND r.dateArriveePrevue < :depart AND r.dateDepartPrevue > :arrivee")
    List<Reservation> findReservationsChevauchantes(@Param("placeId") Long placeId,
                                                    @Param("arrivee") LocalDateTime arrivee,
                                                    @Param("depart") LocalDateTime depart);

    @Query("SELECT r FROM Reservation r WHERE r.vehicule.immatriculation = :immatriculation " +
           "AND r.statut IN ('VALIDEE', 'EN_ATTENTE') " +
           "AND :date BETWEEN r.dateArriveePrevue AND r.dateDepartPrevue")
    Optional<Reservation> findReservationActivePourVehicule(@Param("immatriculation") String immatriculation,
                                                           @Param("date") LocalDateTime date);
}
