package com.tpe.parking.repository;

import java.util.Optional;

import com.tpe.parking.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByTelephone(String telephone);

    /** Recherche paginée sur le nom complet ou l'email (insensible à la casse). */
    @Query("SELECT u FROM Utilisateur u WHERE :recherche = '' " +
           "OR LOWER(CONCAT(COALESCE(u.prenom, ''), ' ', COALESCE(u.nom, ''))) LIKE LOWER(CONCAT('%', :recherche, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :recherche, '%'))")
    Page<Utilisateur> rechercher(@Param("recherche") String recherche, Pageable pageable);
}
