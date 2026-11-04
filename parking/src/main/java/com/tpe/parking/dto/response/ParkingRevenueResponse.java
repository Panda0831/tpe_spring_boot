package com.tpe.parking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParkingRevenueResponse(
        LocalDate dateRapport,
        BigDecimal chiffreAffairesTotal,
        long nombreSortiesFacturees,
        BigDecimal montantMoyenParStationnement
) {}
