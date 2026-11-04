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
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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
    private ReservationStatus statut = ReservationStatus.EN_ATTENTE;

    public Reservation() {
    }

    public Reservation(Long id, Utilisateur utilisateur, Vehicule vehicule, Place place, LocalDateTime dateArriveePrevue, LocalDateTime dateDepartPrevue, ReservationStatus statut) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.vehicule = vehicule;
        this.place = place;
        this.dateArriveePrevue = dateArriveePrevue;
        this.dateDepartPrevue = dateDepartPrevue;
        this.statut = statut;
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
                ", place=" + (place != null ? place.getId() : null) +
                ", dateArriveePrevue=" + dateArriveePrevue +
                ", dateDepartPrevue=" + dateDepartPrevue +
                ", statut=" + statut +
                '}';
    }
}
