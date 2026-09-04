package com.tpe.parking.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // numéro de la place (ex: "A12")
    private String numero;

    // zone du parking (ex: "RDC", "Etage1")
    private String zone;

    @Enumerated(EnumType.STRING)
    private PlaceType type;

    @Enumerated(EnumType.STRING)
    private PlaceStatut statut;

    @OneToMany(mappedBy = "place", fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "place", fetch = FetchType.LAZY)
    private List<EntreeSortie> entreesSorties = new ArrayList<>();

    public Place() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setType(PlaceType type) {
        this.type = type;
    }

    public void setStatut(PlaceStatut statut) {
        this.statut = statut;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public void setEntreesSorties(List<EntreeSortie> entreesSorties) {
        this.entreesSorties = entreesSorties;
    }

    @Override
    public String toString() {
        return "Place{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", zone='" + zone + '\'' +
                ", type=" + type +
                ", statut=" + statut +
                '}';
    }
}
