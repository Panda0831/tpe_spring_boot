package com.tpe.parking.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.tpe.parking.entity.Utilisateur;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UtilisateurDto {

    private Long id;

    @NotBlank(message = "Le nom ne peut pas être vide")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String nom;

    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String prenom;

    @NotBlank(message = "Le numéro de téléphone ne peut pas être vide")
    @Pattern(regexp = "^(0|\\+261)(32|33|34|38)\\d{7}$", message = "Le numéro de téléphone n'est pas valide")
    private String telephone;

    private List<String> vehicules;

    public UtilisateurDto() {
    }

    public UtilisateurDto(Long id, String nom, String prenom, String telephone, List<String> vehicules) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.vehicules = vehicules;
    }

    public List<String> getVehicules() {
        return vehicules;
    }

    public void setVehicules(List<String> vehicules) {
        this.vehicules = vehicules;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return this.prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public UtilisateurDto id(Long id) {
        setId(id);
        return this;
    }

    public UtilisateurDto nom(String nom) {
        setNom(nom);
        return this;
    }

    public UtilisateurDto prenom(String prenom) {
        setPrenom(prenom);
        return this;
    }

    public UtilisateurDto telephone(String telephone) {
        setTelephone(telephone);
        return this;
    }

    public UtilisateurDto vehicules(List<String> vehicules) {
        setVehicules(vehicules);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UtilisateurDto)) {
            return false;
        }
        UtilisateurDto other = (UtilisateurDto) o;
        return Objects.equals(id, other.id)
                && Objects.equals(nom, other.nom)
                && Objects.equals(prenom, other.prenom)
                && Objects.equals(telephone, other.telephone)
                && Objects.equals(vehicules, other.vehicules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom, telephone, vehicules);
    }

    @Override
    public String toString() {
        return "{" +
                " id='" + getId() + "'" +
                ", nom='" + getNom() + "'" +
                ", prenom='" + getPrenom() + "'" +
                ", telephone='" + getTelephone() + "'" +
                ", vehicules='" + getVehicules() + "'" +
                "}";
    }

    public static UtilisateurDto versDto(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        UtilisateurDto utilisateurDto = new UtilisateurDto();
        utilisateurDto.setId(utilisateur.getId());
        utilisateurDto.setNom(utilisateur.getNom());
        utilisateurDto.setPrenom(utilisateur.getPrenom());
        utilisateurDto.setTelephone(utilisateur.getTelephone());

        if (utilisateur.getVehicules() != null) {
            List<String> immatriculations = new ArrayList<>();
            utilisateur.getVehicules().forEach(v -> immatriculations.add(String.valueOf(v.getImmatriculation())));
            utilisateurDto.setVehicules(immatriculations);
        }

        return utilisateurDto;
    }

}
