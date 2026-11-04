package com.tpe.parking.service;

import com.tpe.parking.entity.Role;
import com.tpe.parking.entity.Tarif;
import com.tpe.parking.repository.TarifRepository;
import com.tpe.parking.service.impl.TarifServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Calcul tarifaire : durée, tarif minimum, réduction abonné et surtout majoration
 * des heures de dépassement (exemple du sujet : prévu jusqu'à 13h, sortie à 15h
 * =&gt; 2 heures supplémentaires facturées au tarif majoré).
 */
@ExtendWith(MockitoExtension.class)
class TarifServiceTest {

    @Mock
    private TarifRepository tarifRepository;

    @InjectMocks
    private TarifServiceImpl tarifService;

    /** Tarif de la zone RDC : 2.00/h, minimum 1.50, forfait jour 15.00, -20% abonné, +50% dépassement. */
    private static final Tarif TARIF_RDC = new Tarif(1L, "RDC",
            new BigDecimal("2.00"), new BigDecimal("1.50"), new BigDecimal("15.00"),
            new BigDecimal("0.20"), new BigDecimal("0.50"));

    /** Le tarif de la zone RDC est le seul configuré pour ces tests. */
    private void avecTarifRdc() {
        when(tarifRepository.findFirstByZoneIgnoreCase(anyString())).thenReturn(Optional.of(TARIF_RDC));
    }

    @Test
    @DisplayName("2 heures de stationnement au tarif horaire de la zone")
    void calculSimple() {
        avecTarifRdc();
        BigDecimal montant = tarifService.calculerMontant(
                LocalDateTime.of(2026, 9, 20, 10, 0),
                LocalDateTime.of(2026, 9, 20, 12, 0),
                "RDC", false, Role.USER, null);

        assertThat(montant).isEqualByComparingTo("4.00");
    }

    @Test
    @DisplayName("Le tarif minimum s'applique aux stationnements très courts")
    void tarifMinimumApplique() {
        avecTarifRdc();
        BigDecimal montant = tarifService.calculerMontant(
                LocalDateTime.of(2026, 9, 20, 10, 0),
                LocalDateTime.of(2026, 9, 20, 10, 20),
                "RDC", false, Role.USER, null);

        assertThat(montant).isEqualByComparingTo("1.50");
    }

    @Test
    @DisplayName("Réduction abonné de 20 % appliquée au tarif horaire")
    void reductionAbonne() {
        avecTarifRdc();
        BigDecimal montant = tarifService.calculerMontant(
                LocalDateTime.of(2026, 9, 20, 10, 0),
                LocalDateTime.of(2026, 9, 20, 12, 0),
                "RDC", true, Role.USER, null);

        assertThat(montant).isEqualByComparingTo("3.20");
    }

    @Test
    @DisplayName("Dépassement : 3h prévues, 5h réelles => 2h supplémentaires majorées (+50 %)")
    void depassementFactureSeulementLesHeuresSupplementaires() {
        avecTarifRdc();
        BigDecimal montant = tarifService.calculerMontantAvecDepassement(
                LocalDateTime.of(2026, 9, 20, 12, 0),   // entrée
                LocalDateTime.of(2026, 9, 20, 17, 0),   // sortie réelle (5h)
                LocalDateTime.of(2026, 9, 20, 15, 0),   // départ prévu (3h)
                "RDC", false, Role.USER, null);

        // 3h x 2.00 = 6.00 puis 2h x (2.00 x 1.50) = 6.00 => 12.00 (et non 16.00)
        assertThat(montant).isEqualByComparingTo("12.00");
    }

    @Test
    @DisplayName("Sans dépassement, le montant est celui de la durée réelle")
    void sansDepassement() {
        avecTarifRdc();
        BigDecimal montant = tarifService.calculerMontantAvecDepassement(
                LocalDateTime.of(2026, 9, 20, 12, 0),
                LocalDateTime.of(2026, 9, 20, 14, 0),
                LocalDateTime.of(2026, 9, 20, 15, 0),
                "RDC", false, Role.USER, null);

        assertThat(montant).isEqualByComparingTo("4.00");
    }

    @Test
    @DisplayName("Une sortie antérieure à l'entrée ne produit aucune facturation")
    void sortieIncoherente() {
        BigDecimal montant = tarifService.calculerMontant(
                LocalDateTime.of(2026, 9, 20, 12, 0),
                LocalDateTime.of(2026, 9, 20, 11, 0),
                "RDC", false, Role.USER, null);

        assertThat(montant).isEqualByComparingTo("0.00");
    }
}
