package com.tpe.parking.mapper;

import com.tpe.parking.dto.AbonnementDto;
import com.tpe.parking.entity.Abonnement;
import com.tpe.parking.entity.AbonnementType;

import java.time.LocalDate;

public class AbonnementMapper {
    private AbonnementMapper() {
    }

    public static AbonnementDto toDto(Abonnement abonnement) {
        if (abonnement == null) {
            return null;
        }
        return new AbonnementDto(
                abonnement.getId(),
                abonnement.getType() != null ? abonnement.getType().name() : null,
                abonnement.getVehicule(),
                abonnement.getStatut() != null ? abonnement.getStatut().name() : null,
                abonnement.getUtilisateur(),
                abonnement.getDateDebut() != null ? abonnement.getDateDebut().toString() : null,
                abonnement.getDateFin() != null ? abonnement.getDateFin().toString() : null
        );
    }

    public static Abonnement toEntity(AbonnementDto abonnementDto) {
        if (abonnementDto == null) {
            return null;
        }
        Abonnement abonnement = new Abonnement();
        abonnement.setId(abonnementDto.getId());
        abonnement.setType(abonnementDto.getType() != null ? Enum.valueOf(AbonnementType.class, abonnementDto.getType()) : null);
        abonnement.setVehicule(abonnementDto.getVehicule());
        abonnement.setUtilisateur(abonnementDto.getUtilisateur());
        abonnement.setDateDebut(abonnementDto.getDateDebut() != null ? LocalDate.parse(abonnementDto.getDateDebut()) : null);
        abonnement.setDateFin(abonnementDto.getDateFin() != null ? LocalDate.parse(abonnementDto.getDateFin()) : null);
        return abonnement;
    }
}

