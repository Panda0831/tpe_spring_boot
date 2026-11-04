package com.tpe.taxis.repository;

import com.tpe.taxis.entity.ParametreTarif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametreTarifRepository extends JpaRepository<ParametreTarif, Long> {
}
