package com.tpe.parking.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // immatriculation
    private long immatriculation;

    private String marque;

    // modele
    private String modele;

    @ManyToOne(fetch = FetchType.LAZY)
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Abonnement> abonnements = new ArrayList<>();

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EntreeSortie> entreesSorties = new ArrayList<>();

    public Vehicule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(long immatriculation) {
        this.immatriculation = immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public List<Abonnement> getAbonnements() {
        return abonnements;
    }

    public void setAbonnements(List<Abonnement> abonnements) {
        this.abonnements = abonnements;
    }

    public List<EntreeSortie> getEntreesSorties() {
        return entreesSorties;
    }

    public void setEntreesSorties(List<EntreeSortie> entreesSorties) {
        this.entreesSorties = entreesSorties;
    }

    @Override
    public String toString() {
        return "{" +
                " id='" + getId() + "'" +
                ", immatriculation='" + getImmatriculation() + "'" +
                ", marque='" + getMarque() + "'" +
                ", modele='" + getModele() + "'" +
                "}";
    }
}
