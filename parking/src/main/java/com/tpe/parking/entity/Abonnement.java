package com.tpe.parking.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    private Vehicule vehicule;

    @Enumerated(EnumType.STRING)
    private AbonnementType type;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private AbonnementStatut statut;

    public Abonnement() {
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

    public AbonnementType getType() {
        return type;
    }

    public void setType(AbonnementType type) {
        this.type = type;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public AbonnementStatut getStatut() {
        return statut;
    }

    public void setStatut(AbonnementStatut statut) {
        this.statut = statut;
    }

    /**
     * Détermine si l'abonnement est actuellement valide en fonction des dates.
     */
    public boolean estValide() {
        if (dateDebut == null || dateFin == null) return false;
        LocalDate now = LocalDate.now();
        return (now.isEqual(dateDebut) || now.isAfter(dateDebut)) && (now.isEqual(dateFin) || now.isBefore(dateFin)) && this.statut == AbonnementStatut.ACTIF;
    }

    @Override
    public String toString() {
        return "Abonnement{" +
                "id=" + id +
                ", utilisateur=" + (utilisateur != null ? utilisateur.getId() : null) +
                ", vehicule=" + (vehicule != null ? vehicule.getId() : null) +
                ", type=" + type +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", statut=" + statut +
                '}';
    }
}
