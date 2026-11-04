package com.tpe.parking.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

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

    // majoration appliquée après dépassement (pourcentage, ex: 0.50 = +50%)
    private BigDecimal majorationApres;

    public Tarif() {
    }

    public Tarif(Long id, String zone, BigDecimal montantParHeure, BigDecimal tarifMinimum, BigDecimal forfaitJournalier, BigDecimal reductionAbonne, BigDecimal majorationApres) {
        this.id = id;
        this.zone = zone;
        this.montantParHeure = montantParHeure;
        this.tarifMinimum = tarifMinimum;
        this.forfaitJournalier = forfaitJournalier;
        this.reductionAbonne = reductionAbonne;
        this.majorationApres = majorationApres;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public BigDecimal getMontantParHeure() {
        return montantParHeure;
    }

    public void setMontantParHeure(BigDecimal montantParHeure) {
        this.montantParHeure = montantParHeure;
    }

    public BigDecimal getTarifMinimum() {
        return tarifMinimum;
    }

    public void setTarifMinimum(BigDecimal tarifMinimum) {
        this.tarifMinimum = tarifMinimum;
    }

    public BigDecimal getForfaitJournalier() {
        return forfaitJournalier;
    }

    public void setForfaitJournalier(BigDecimal forfaitJournalier) {
        this.forfaitJournalier = forfaitJournalier;
    }

    public BigDecimal getReductionAbonne() {
        return reductionAbonne;
    }

    public void setReductionAbonne(BigDecimal reductionAbonne) {
        this.reductionAbonne = reductionAbonne;
    }

    public BigDecimal getMajorationApres() {
        return majorationApres;
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
