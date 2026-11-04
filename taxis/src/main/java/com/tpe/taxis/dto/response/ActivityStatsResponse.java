package com.tpe.taxis.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ActivityStatsResponse(
        long totalCourses,
        long coursesTerminees,
        long coursesAnnulees,
        long coursesEnCours,
        long coursesEnAttente,
        long coursesAssignees,
        double tauxAnnulation,
        Double distanceTotaleKm,
        BigDecimal chiffreAffaires,
        BigDecimal revenuMoyenParCourse,
        String chauffeurPlusActif,
        Long chauffeurPlusActifId,
        long chauffeurPlusActifNbCourses,
        String vehiculePlusUtilise,
        Long vehiculePlusUtiliseId,
        long vehiculePlusUtiliseNbCourses,
        LocalDateTime periodeDebut,
        LocalDateTime periodeFin
) {
}
