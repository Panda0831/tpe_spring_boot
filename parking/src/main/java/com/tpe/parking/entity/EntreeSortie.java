package com.tpe.parking.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class EntreeSortie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Vehicule vehicule;

    @ManyToOne(fetch = FetchType.LAZY)
    private Place place;

    private LocalDateTime dateEntree;

    private LocalDateTime dateSortie;

    // Date de départ prévue (issue de la réservation) : sert au calcul du dépassement
    private LocalDateTime dateDepartPrevue;

    private BigDecimal montant;

    public EntreeSortie() {
    }

    public EntreeSortie(Long id, Vehicule vehicule, Place place, LocalDateTime dateEntree, LocalDateTime dateSortie, BigDecimal montant) {
        this.id = id;
        this.vehicule = vehicule;
        this.place = place;
        this.dateEntree = dateEntree;
        this.dateSortie = dateSortie;
        this.montant = montant;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public LocalDateTime getDateEntree() {
        return dateEntree;
    }

    public void setDateEntree(LocalDateTime dateEntree) {
        this.dateEntree = dateEntree;
    }

    public LocalDateTime getDateSortie() {
        return dateSortie;
    }

    public void setDateSortie(LocalDateTime dateSortie) {
        this.dateSortie = dateSortie;
    }

    public LocalDateTime getDateDepartPrevue() {
        return dateDepartPrevue;
    }

    public void setDateDepartPrevue(LocalDateTime dateDepartPrevue) {
        this.dateDepartPrevue = dateDepartPrevue;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    @Override
    public String toString() {
        return "EntreeSortie{" +
                "id=" + id +
                ", vehicule=" + (vehicule != null ? vehicule.getId() : null) +
                ", place=" + (place != null ? place.getId() : null) +
                ", dateEntree=" + dateEntree +
                ", dateSortie=" + dateSortie +
                ", montant=" + montant +
                '}';
    }
}
