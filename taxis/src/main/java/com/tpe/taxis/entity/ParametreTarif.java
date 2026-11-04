package com.tpe.taxis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "parametres_tarif")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametreTarif {

    /** Poids par défaut du critère de proximité dans le score d'attribution des courses. */
    public static final BigDecimal DEFAULT_POIDS_PROXIMITE = new BigDecimal("0.60");

    /** Poids par défaut du critère d'équité dans le score d'attribution des courses. */
    public static final BigDecimal DEFAULT_POIDS_EQUITE = new BigDecimal("0.40");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifBase; // Prise en charge initiale

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixKm; // Prix par kilomètre

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPourcentage; // Pourcentage de commission prélevé par l'entreprise (ex: 20.00%)

    /**
     * Poids du critère de proximité (distance entre la dernière position connue du chauffeur
     * et le point de départ de la course) dans le score d'attribution automatique.
     */
    @Column(name = "poids_proximite", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal poidsProximite = DEFAULT_POIDS_PROXIMITE;

    /**
     * Poids du critère d'équité (nombre de courses récentes déjà affectées au chauffeur)
     * dans le score d'attribution automatique.
     */
    @Column(name = "poids_equite", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal poidsEquite = DEFAULT_POIDS_EQUITE;
}
