package com.tpe.taxis.repository;

import java.util.Optional;

import com.tpe.taxis.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Page<Client> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(
            String nom, String prenom, Pageable pageable);

    Optional<Client> findByTelephone(String telephone);

    Optional<Client> findByEmail(String email);

    boolean existsByTelephone(String telephone);

    @Query("SELECT c FROM Client c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(c.prenom) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(c.telephone) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Client> rechercher(@Param("q") String q, Pageable pageable);
}
