package com.tpe.taxis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chauffeurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chauffeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false, unique = true)
    private String numeroPermis;

    @Column(nullable = false)
    private LocalDate dateExpirationPermis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ChauffeurStatut statut = ChauffeurStatut.DISPONIBLE;

    @Column(nullable = false)
    private LocalDate dateEmbauche;

    @OneToOne(mappedBy = "chauffeurActuel")
    private Vehicule vehicule;

    @OneToMany(mappedBy = "chauffeur")
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    public boolean isPermisValide() {
        return dateExpirationPermis != null && !dateExpirationPermis.isBefore(LocalDate.now());
    }

    public boolean isEligiblePourCourse() {
        return statut == ChauffeurStatut.DISPONIBLE && isPermisValide();
    }
}
