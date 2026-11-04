package com.tpe.taxis.repository;

import java.util.Optional;

import com.tpe.taxis.entity.CompteUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompteUtilisateurRepository extends JpaRepository<CompteUtilisateur, Long> {

    Optional<CompteUtilisateur> findByUsername(String username);

    boolean existsByUsername(String username);
}
