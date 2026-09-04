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

@Getter
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

    private BigDecimal montant;

    public EntreeSortie() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public void setDateEntree(LocalDateTime dateEntree) {
        this.dateEntree = dateEntree;
    }

    public void setDateSortie(LocalDateTime dateSortie) {
        this.dateSortie = dateSortie;
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
