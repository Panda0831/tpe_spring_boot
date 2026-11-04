package com.tpe.taxis.service.impl;

import com.tpe.taxis.dto.response.ActivityStatsResponse;
import com.tpe.taxis.dto.response.RevenueStatsResponse;
import com.tpe.taxis.repository.CourseRepository;
import com.tpe.taxis.repository.VehiculeRepository;
import com.tpe.taxis.service.TaxiStatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TaxiStatistiqueServiceImpl implements TaxiStatistiqueService {

    private final CourseRepository courseRepository;
    private final VehiculeRepository vehiculeRepository;

    @Override
    public RevenueStatsResponse getRevenueStats(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal ca = courseRepository.sumCaByPeriode(debut, fin);
        BigDecimal commission = courseRepository.sumCommissionByPeriode(debut, fin);
        BigDecimal montantChauffeurs = courseRepository.sumMontantChauffeurByPeriode(debut, fin);
        Double distance = courseRepository.sumDistanceByPeriode(debut, fin);

        List<RevenueStatsResponse.ChauffeurRevenueDto> caParChauffeur = new ArrayList<>();
        for (Object[] row : courseRepository.sumCaParChauffeur(debut, fin)) {
            caParChauffeur.add(new RevenueStatsResponse.ChauffeurRevenueDto(
                    (Long) row[0],
                    buildNom((String) row[2], (String) row[1]),
                    toBigDecimal(row[3]),
                    (Long) row[4]
            ));
        }

        List<RevenueStatsResponse.VehiculeRevenueDto> caParVehicule = new ArrayList<>();
        for (Object[] row : courseRepository.sumCaParVehicule(debut, fin)) {
            caParVehicule.add(new RevenueStatsResponse.VehiculeRevenueDto(
                    (Long) row[0],
                    (String) row[1],
                    toBigDecimal(row[2]),
                    (Long) row[3]
            ));
        }

        long nbCourses = caParChauffeur.stream().mapToLong(RevenueStatsResponse.ChauffeurRevenueDto::nombreCourses).sum();

        return new RevenueStatsResponse(
                ca,
                commission,
                montantChauffeurs,
                nbCourses,
                moyenne(ca, nbCourses),
                debut,
                fin,
                caParChauffeur,
                caParVehicule
        );
    }

    @Override
    public ActivityStatsResponse getActivityStats(LocalDateTime debut, LocalDateTime fin) {
        long totalCourses = 0;
        long terminees = 0;
        long annulees = 0;
        long enCours = 0;
        long enAttente = 0;
        long assignees = 0;

        for (Object[] row : courseRepository.countByStatutBetween(debut, fin)) {
            com.tpe.taxis.entity.CourseStatut statut = (com.tpe.taxis.entity.CourseStatut) row[0];
            long count = (Long) row[1];
            totalCourses += count;
            switch (statut) {
                case TERMINEE -> terminees = count;
                case ANNULEE -> annulees = count;
                case EN_COURS -> enCours = count;
                case EN_ATTENTE -> enAttente = count;
                case ASSIGNEE -> assignees = count;
            }
        }

        Double distanceTotale = courseRepository.sumDistanceByPeriode(debut, fin);
        BigDecimal ca = courseRepository.sumCaByPeriode(debut, fin);

        String chauffeurPlusActif = null;
        Long chauffeurPlusActifId = null;
        long chauffeurPlusActifNb = 0;
        List<RevenueStatsResponse.ChauffeurRevenueDto> parChauffeur = new ArrayList<>();
        for (Object[] row : courseRepository.sumCaParChauffeur(debut, fin)) {
            parChauffeur.add(new RevenueStatsResponse.ChauffeurRevenueDto(
                    (Long) row[0],
                    buildNom((String) row[2], (String) row[1]),
                    toBigDecimal(row[3]),
                    (Long) row[4]
            ));
        }
        if (!parChauffeur.isEmpty()) {
            RevenueStatsResponse.ChauffeurRevenueDto top = parChauffeur.get(0);
            chauffeurPlusActifId = top.chauffeurId();
            chauffeurPlusActif = top.nomChauffeur();
            chauffeurPlusActifNb = top.nombreCourses();
        }

        String vehiculePlusUtilise = null;
        Long vehiculePlusUtiliseId = null;
        long vehiculePlusUtiliseNb = 0;
        List<RevenueStatsResponse.VehiculeRevenueDto> parVehicule = new ArrayList<>();
        for (Object[] row : courseRepository.sumCaParVehicule(debut, fin)) {
            parVehicule.add(new RevenueStatsResponse.VehiculeRevenueDto(
                    (Long) row[0],
                    (String) row[1],
                    toBigDecimal(row[2]),
                    (Long) row[3]
            ));
        }
        if (!parVehicule.isEmpty()) {
            RevenueStatsResponse.VehiculeRevenueDto top = parVehicule.get(0);
            vehiculePlusUtiliseId = top.vehiculeId();
            vehiculePlusUtilise = top.immatriculation();
            vehiculePlusUtiliseNb = top.nombreCourses();
        }

        double tauxAnnulation = totalCourses > 0 ? (annulees * 100.0) / totalCourses : 0.0;

        return new ActivityStatsResponse(
                totalCourses,
                terminees,
                annulees,
                enCours,
                enAttente,
                assignees,
                tauxAnnulation,
                distanceTotale,
                ca,
                moyenne(ca, terminees),
                chauffeurPlusActif,
                chauffeurPlusActifId,
                chauffeurPlusActifNb,
                vehiculePlusUtilise,
                vehiculePlusUtiliseId,
                vehiculePlusUtiliseNb,
                debut,
                fin
        );
    }

    private String buildNom(String nom, String prenom) {
        return ((prenom != null ? prenom : "") + " " + (nom != null ? nom : "")).trim();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal moyenne(BigDecimal total, long nombre) {
        if (nombre <= 0 || total == null) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(nombre), 2, RoundingMode.HALF_UP);
    }
}
