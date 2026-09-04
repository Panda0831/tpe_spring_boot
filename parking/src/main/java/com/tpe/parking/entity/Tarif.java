package com.tpe.parking.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Représentation simplifiée d'un tarif.
 * Le calcul réel pourra prendre en compte de multiples facteurs ;
 * ce modèle stocke des paramètres de base.
 */
@Getter
@Setter
@Entity
public class Tarif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // zone à laquelle s'applique le tarif (nullable pour tarif global)
    private String zone;

    // montant par heure
    private BigDecimal montantParHeure;

    // tarif minimum
    private BigDecimal tarifMinimum;

    // forfait journalier (optionnel)
    private BigDecimal forfaitJournalier;

    // réduction pour abonnés (pourcentage, ex: 0.20 = 20%)
    private BigDecimal reductionAbonne;

    // majoration appliquée après dépassement (pourcentage)
    private BigDecimal majorationApres;

    public Tarif() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setMontantParHeure(BigDecimal montantParHeure) {
        this.montantParHeure = montantParHeure;
    }

    public void setTarifMinimum(BigDecimal tarifMinimum) {
        this.tarifMinimum = tarifMinimum;
    }

    public void setForfaitJournalier(BigDecimal forfaitJournalier) {
        this.forfaitJournalier = forfaitJournalier;
    }

    public void setReductionAbonne(BigDecimal reductionAbonne) {
        this.reductionAbonne = reductionAbonne;
    }

    public void setMajorationApres(BigDecimal majorationApres) {
        this.majorationApres = majorationApres;
    }

    @Override
    public String toString() {
        return "Tarif{" +
                "id=" + id +
                ", zone='" + zone + '\'' +
                ", montantParHeure=" + montantParHeure +
                ", tarifMinimum=" + tarifMinimum +
                ", forfaitJournalier=" + forfaitJournalier +
                ", reductionAbonne=" + reductionAbonne +
                ", majorationApres=" + majorationApres +
                '}';
    }
}
