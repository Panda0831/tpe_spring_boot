package com.tpe.taxis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String immatriculation;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    private Integer annee;

    @Column(nullable = false)
    private Integer capacite;

    @Column(nullable = false)
    @Builder.Default
    private Double kilometrage = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VehiculeStatut statut = VehiculeStatut.DISPONIBLE;

    @OneToOne
    @JoinColumn(name = "chauffeur_id", unique = true)
    private Chauffeur chauffeurActuel;

    @OneToMany(mappedBy = "vehicule")
    @Builder.Default
    private List<Course> courses = new ArrayList<>();
}
