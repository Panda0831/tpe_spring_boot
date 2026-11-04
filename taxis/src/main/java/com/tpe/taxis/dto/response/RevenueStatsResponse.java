package com.tpe.taxis.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RevenueStatsResponse(
        BigDecimal chiffreAffairesTotal,
        BigDecimal commissionTotale,
        BigDecimal montantChauffeursTotal,
        long nombreCoursesTerminees,
        BigDecimal revenuMoyenParCourse,
        LocalDateTime periodeDebut,
        LocalDateTime periodeFin,
        List<ChauffeurRevenueDto> caParChauffeur,
        List<VehiculeRevenueDto> caParVehicule
) {
    public record ChauffeurRevenueDto(
            Long chauffeurId,
            String nomChauffeur,
            BigDecimal chiffreAffaires,
            long nombreCourses
    ) {}

    public record VehiculeRevenueDto(
            Long vehiculeId,
            String immatriculation,
            BigDecimal chiffreAffaires,
            long nombreCourses
    ) {}
}
