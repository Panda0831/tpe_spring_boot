package com.tpe.parking.repository;

import com.tpe.parking.entity.Tarif;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifRepository extends JpaRepository<Tarif, Long> {
}
