package com.tpe.parking.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    private Vehicule vehicule;

    @ManyToOne(fetch = FetchType.LAZY)
    private Place place;

    private LocalDateTime dateArriveePrevue;

    private LocalDateTime dateDepartPrevue;

    @Enumerated(EnumType.STRING)
    private ReservationStatus statut;

    public Reservation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
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

    public LocalDateTime getDateArriveePrevue() {
        return dateArriveePrevue;
    }

    public void setDateArriveePrevue(LocalDateTime dateArriveePrevue) {
        this.dateArriveePrevue = dateArriveePrevue;
    }

    public LocalDateTime getDateDepartPrevue() {
        return dateDepartPrevue;
    }

    public void setDateDepartPrevue(LocalDateTime dateDepartPrevue) {
        this.dateDepartPrevue = dateDepartPrevue;
    }

    public ReservationStatus getStatut() {
        return statut;
    }

    public void setStatut(ReservationStatus statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", utilisateur=" + (utilisateur != null ? utilisateur.getId() : null) +
                ", vehicule=" + (vehicule != null ? vehicule.getId() : null) +
                ", place=" + (place != null ? place.toString() : null) +
                ", dateArriveePrevue=" + dateArriveePrevue +
                ", dateDepartPrevue=" + dateDepartPrevue +
                ", statut=" + statut +
                '}';
    }
}
