package com.tpe.taxis.service;

import com.tpe.taxis.dto.response.ActivityStatsResponse;
import com.tpe.taxis.dto.response.RevenueStatsResponse;

import java.time.LocalDateTime;

public interface TaxiStatistiqueService {

    RevenueStatsResponse getRevenueStats(LocalDateTime debut, LocalDateTime fin);

    ActivityStatsResponse getActivityStats(LocalDateTime debut, LocalDateTime fin);
}
