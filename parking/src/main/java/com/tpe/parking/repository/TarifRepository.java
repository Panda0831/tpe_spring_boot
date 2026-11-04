package com.tpe.parking.repository;

import java.util.Optional;

import com.tpe.parking.entity.Tarif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifRepository extends JpaRepository<Tarif, Long> {

    Optional<Tarif> findByZone(String zone);

    Optional<Tarif> findFirstByZoneIgnoreCase(String zone);
}
