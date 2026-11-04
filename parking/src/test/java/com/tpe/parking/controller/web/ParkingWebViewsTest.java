package com.tpe.parking.controller.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Interface Web Thymeleaf : chaque écran doit se rendre avec les données réelles de la base,
 * les écritures sont protégées par le rôle et par le jeton CSRF.
 *
 * <p>La chaîne de filtres Spring Security est appliquée à MockMvc (configurer
 * {@code springSecurity()}) afin que {@code @WithMockUser} et le filtre CSRF jouent
 * exactement le même rôle qu'en production.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ParkingWebViewsTest {

    @TestConfiguration
    static class SecuriteMockMvc {
        @Bean
        MockMvcBuilderCustomizer securiteSpring() {
            return builder -> builder.apply(springSecurity());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("La page de connexion est accessible sans authentification")
    void pageDeConnexion() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(containsString("Se connecter")))
                .andExpect(content().string(containsString("favicon.svg")));
    }

    @Test
    @DisplayName("Le favicon est public et une icône absente retourne 404, pas 500")
    void faviconEtRessourceAbsente() throws Exception {
        mockMvc.perform(get("/favicon.svg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"));

        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Un compte de démonstration peut ouvrir une session")
    void connexionAvecCompteDemo() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin@parking.com")
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("Toute page protégée redirige vers /login sans session")
    void redirectionVersLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @ParameterizedTest
    @CsvSource({
            "/dashboard, dashboard",
            "/places, places",
            "/reservations, reservations",
            "/abonnements, abonnements",
            "/borne, borne",
            "/statistiques, statistiques",
            "/tarifs, tarifs",
            "/utilisateurs, utilisateurs",
            "/vehicules, vehicules"
    })
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("Chaque écran se rend (200) et utilise le bon template")
    void ecransSeRendent(String url, String vue) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(vue));
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("Le plan du parking affiche la grille colorée des 24 places")
    void planDuParking() throws Exception {
        mockMvc.perform(get("/places"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("place-card place-LIBRE")))
                .andExpect(content().string(containsString("place-card place-OCCUPEE")))
                .andExpect(content().string(containsString("place-card place-RESERVEE")))
                .andExpect(content().string(containsString("Types :")))
                .andExpect(content().string(not(containsString("Ajouter une place"))));
    }

    @Test
    @WithMockUser(username = "admin@parking.com", roles = "ADMIN")
    @DisplayName("Un administrateur peut créer une place, initialement libre")
    void adminCreeUnePlace() throws Exception {
        mockMvc.perform(post("/places")
                        .param("numero", "c-25")
                        .param("zone", "RDC")
                        .param("type", "STANDARD")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/places"));

        mockMvc.perform(get("/places"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("C-25")))
                .andExpect(content().string(containsString("place-card place-LIBRE")))
                .andExpect(content().string(containsString("Ajouter une place")));
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("Un opérateur ne peut pas créer de place")
    void operateurNeCreePasUnePlace() throws Exception {
        mockMvc.perform(post("/places")
                        .param("numero", "C25")
                        .param("zone", "RDC")
                        .param("type", "STANDARD")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("Le plan du parking filtre les places par zone")
    void planDuParkingFiltre() throws Exception {
        mockMvc.perform(get("/places").param("zone", "Etage1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Zone Etage1")))
                .andExpect(content().string(not(containsString("Zone RDC"))));
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("Le tableau de bord affiche les indicateurs et l'occupation par zone")
    void tableauDeBord() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Taux d'occupation")))
                .andExpect(content().string(containsString("Occupation par zone")))
                .andExpect(content().string(containsString("RDC")))
                .andExpect(content().string(containsString("Chiffre d'affaires du jour")));
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("La borne affiche le véhicule présent et l'historique")
    void borne() throws Exception {
        mockMvc.perform(get("/borne"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enregistrer une entrée")))
                .andExpect(content().string(containsString("Enregistrer une sortie")))
                .andExpect(content().string(containsString("CD-AAA-001")))
                .andExpect(content().string(containsString("En cours")));
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("La liste des réservations propose le formulaire de création")
    void reservations() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nouvelle réservation")))
                .andExpect(content().string(containsString("CD-AAA-001")));
    }

    @Test
    @WithMockUser(username = "manager@parking.com", roles = "MANAGER")
    @DisplayName("Les statistiques affichent l'occupation par zone et l'histogramme des revenus")
    void statistiques() throws Exception {
        mockMvc.perform(get("/statistiques").param("jours", "7"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Taux d'occupation par zone")))
                .andExpect(content().string(containsString("Chiffre d'affaires des derniers jours")))
                .andExpect(content().string(containsString("barre-revenu")));
    }

    @Test
    @WithMockUser(username = "manager@parking.com", roles = "MANAGER")
    @DisplayName("Les abonnements affichent la validité calculée par le domaine")
    void abonnements() throws Exception {
        mockMvc.perform(get("/abonnements"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Souscrire un abonnement")))
                .andExpect(content().string(containsString("Valide")))
                .andExpect(content().string(containsString("Non valide")));
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("Un formulaire sans jeton CSRF est refusé")
    void csrfObligatoire() throws Exception {
        mockMvc.perform(post("/borne/entree").param("immatriculation", "CD-AAA-002"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("L'entrée d'un véhicule à la borne crée le passage et occupe une place")
    void entreeDepuisLaBorne() throws Exception {
        mockMvc.perform(post("/borne/entree")
                        .param("immatriculation", "CD-AAA-002")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/borne"))
                .andExpect(flash().attributeExists("ticket", "succes"));
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("Un véhicule inconnu est refusé à la borne avec un message d'erreur")
    void entreeVehiculeInconnu() throws Exception {
        mockMvc.perform(post("/borne/entree")
                        .param("immatriculation", "XX-999-XX")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/borne"))
                .andExpect(flash().attribute("erreur", containsString("XX-999-XX")));
    }

    @Test
    @WithMockUser(username = "user@parking.com", roles = "USER")
    @DisplayName("Un USER ne peut pas enregistrer d'entrée à la borne (403)")
    void userNePeutPasOpererLaBorne() throws Exception {
        mockMvc.perform(post("/borne/entree")
                        .param("immatriculation", "CD-AAA-002")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@parking.com", roles = "USER")
    @DisplayName("Un USER ne peut pas souscrire un abonnement (403)")
    void userNePeutPasSouscrire() throws Exception {
        mockMvc.perform(post("/abonnements")
                        .param("utilisateurId", "4")
                        .param("vehiculeId", "1")
                        .param("type", "MENSUEL")
                        .param("dateDebut", "2027-01-01")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager@parking.com", roles = "MANAGER")
    @DisplayName("Un MANAGER peut souscrire un abonnement depuis l'interface Web")
    void managerPeutSouscrire() throws Exception {
        mockMvc.perform(post("/abonnements")
                        .param("utilisateurId", "4")
                        .param("vehiculeId", "4")
                        .param("type", "MENSUEL")
                        .param("dateDebut", "2027-01-01")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/abonnements"))
                .andExpect(flash().attribute("succes", containsString("Abonnement souscrit")));
    }

    @Test
    @WithUserDetails("user@parking.com")
    @DisplayName("Un USER peut consulter le plan et créer une réservation")
    void userReserveUnePlace() throws Exception {
        mockMvc.perform(get("/places"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/reservations")
                        .param("utilisateurId", "4")
                        .param("vehiculeId", "2")
                        .param("placeId", "9")
                        .param("dateArriveePrevue", "2027-06-01T10:00")
                        .param("dateDepartPrevue", "2027-06-01T12:00")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations"))
                .andExpect(flash().attribute("succes", containsString("créée")));
    }

    @Test
    @WithUserDetails("user@parking.com")
    @DisplayName("USER ne voit que ses véhicules et n'accède pas aux écrans de gestion")
    void visibiliteUtilisateurEtPagesInterdites() throws Exception {
        mockMvc.perform(get("/vehicules"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CD-AAA-001")))
                .andExpect(content().string(not(containsString("CD-AAA-003"))))
                .andExpect(content().string(not(containsString("Utilisateurs"))));

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CD-AAA-001")))
                .andExpect(content().string(not(containsString("CD-AAA-003"))))
                .andExpect(content().string(not(containsString("href=\"/utilisateurs\""))))
                .andExpect(content().string(not(containsString("href=\"/tarifs\""))));

        mockMvc.perform(get("/utilisateurs")).andExpect(status().isForbidden());
        mockMvc.perform(get("/abonnements")).andExpect(status().isForbidden());
        mockMvc.perform(get("/borne")).andExpect(status().isForbidden());
        mockMvc.perform(get("/statistiques")).andExpect(status().isForbidden());
        mockMvc.perform(get("/tarifs")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("user@parking.com")
    @DisplayName("USER ne peut pas réserver au nom d'un autre utilisateur")
    void userNePeutPasUsurperUtilisateurReservation() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("utilisateurId", "5")
                        .param("vehiculeId", "3")
                        .param("dateArriveePrevue", "2027-06-01T10:00")
                        .param("dateDepartPrevue", "2027-06-01T12:00")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("Le formulaire de réservation fournit les véhicules avec leur propriétaire pour filtrage")
    void vehiculesFiltresParUtilisateurPourReservation() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-utilisateur-id=\"4\"")))
                .andExpect(content().string(containsString("data-utilisateur-id=\"5\"")))
                .andExpect(content().string(containsString("const vehiculesUtilisateur = vehiculesDisponibles.filter")));
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("Une réservation refuse un véhicule qui appartient à un autre utilisateur")
    void reservationRefuseVehiculeAutreUtilisateur() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("utilisateurId", "4")
                        .param("vehiculeId", "3")
                        .param("dateArriveePrevue", "2027-06-01T10:00")
                        .param("dateDepartPrevue", "2027-06-01T12:00")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations"))
                .andExpect(flash().attribute("erreur",
                        containsString("n'appartient pas à l'utilisateur choisi")));
    }

    @Test
    @WithMockUser(username = "user@parking.com", roles = "USER")
    @DisplayName("Un USER ne peut pas annuler une réservation (403)")
    void userNePeutPasAnnuler() throws Exception {
        mockMvc.perform(post("/reservations/1/annuler").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("Un OPERATOR peut annuler une réservation")
    void operatorAnnuleReservation() throws Exception {
        mockMvc.perform(post("/reservations/1/annuler").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations"))
                .andExpect(flash().attribute("succes", containsString("annulée")));
    }

    @Test
    @WithMockUser(username = "manager@parking.com", roles = "MANAGER")
    @DisplayName("Un MANAGER ne peut pas résilier un abonnement (réservé à ADMIN)")
    void managerNePeutPasResilier() throws Exception {
        mockMvc.perform(post("/abonnements/1/resilier").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@parking.com", roles = "ADMIN")
    @DisplayName("Un ADMIN peut résilier un abonnement")
    void adminResilieAbonnement() throws Exception {
        mockMvc.perform(post("/abonnements/1/resilier").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/abonnements"))
                .andExpect(flash().attribute("succes", containsString("résilié")));
    }

    @Test
    @WithMockUser(username = "admin@parking.com", roles = "ADMIN")
    @DisplayName("Un ADMIN crée un tarif par zone depuis l'interface web")
    void adminCreeTarif() throws Exception {
        mockMvc.perform(post("/tarifs")
                        .param("zone", "ZONE_WEB")
                        .param("montantParHeure", "3.50")
                        .param("tarifMinimum", "1.50")
                        .param("forfaitJournalier", "25.00")
                        .param("reductionAbonne", "0.25")
                        .param("majorationApres", "1.50")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tarifs"))
                .andExpect(flash().attribute("succes", containsString("créée")));
    }

    @Test
    @WithMockUser(username = "user@parking.com", roles = "USER")
    @DisplayName("Un USER ne peut pas créer de tarif (403)")
    void userNePeutPasCreerTarif() throws Exception {
        mockMvc.perform(post("/tarifs")
                        .param("zone", "ZONE_INTERDITE")
                        .param("montantParHeure", "2.00")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager@parking.com", roles = "MANAGER")
    @DisplayName("Un MANAGER crée un utilisateur et un véhicule depuis l'interface web")
    void managerCreeUtilisateurEtVehicule() throws Exception {
        mockMvc.perform(post("/utilisateurs")
                        .param("nom", "Durand")
                        .param("prenom", "Alice")
                        .param("telephone", "0611223344")
                        .param("email", "alice.durand.web@parking.com")
                        .param("role", "USER")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/utilisateurs"))
                .andExpect(flash().attribute("succes", containsString("créé")));

        mockMvc.perform(post("/vehicules")
                        .param("immatriculation", "WEB-999-ZZ")
                        .param("marque", "Renault")
                        .param("modele", "Clio")
                        .param("utilisateurId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vehicules"))
                .andExpect(flash().attribute("succes", containsString("enregistré")));
    }

    @Test
    @WithMockUser(username = "manager@parking.com", roles = "MANAGER")
    @DisplayName("La création d'un utilisateur refuse un téléphone invalide")
    void telephoneInvalideRefuse() throws Exception {
        mockMvc.perform(post("/utilisateurs")
                        .param("nom", "Durand")
                        .param("telephone", "abc123")
                        .param("email", "invalid.phone@parking.com")
                        .param("role", "USER")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/utilisateurs"))
                .andExpect(flash().attribute("erreur", containsString("10 chiffres")));
    }

    @Test
    @WithMockUser(username = "manager@parking.com", roles = "MANAGER")
    @DisplayName("La création d'un véhicule refuse une immatriculation invalide")
    void immatriculationInvalideRefusee() throws Exception {
        mockMvc.perform(post("/vehicules")
                        .param("immatriculation", "!!!")
                        .param("utilisateurId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vehicules"))
                .andExpect(flash().attributeExists("erreur"));
    }

    @Test
    @WithMockUser(username = "user@parking.com", roles = "USER")
    @DisplayName("Une réservation dont l'arrivée suit le départ est refusée")
    void periodeReservationInvalideRefusee() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("utilisateurId", "4")
                        .param("vehiculeId", "2")
                        .param("dateArriveePrevue", "2027-06-01T12:00")
                        .param("dateDepartPrevue", "2027-06-01T10:00")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations"))
                .andExpect(flash().attribute("erreur", containsString("antérieure")));
    }

    @Test
    @WithMockUser(username = "manager@parking.com", roles = "MANAGER")
    @DisplayName("Une fin d'abonnement antérieure au début est refusée")
    void periodeAbonnementInvalideRefusee() throws Exception {
        mockMvc.perform(post("/abonnements")
                        .param("utilisateurId", "4")
                        .param("vehiculeId", "4")
                        .param("type", "MENSUEL")
                        .param("dateDebut", "2027-01-01")
                        .param("dateFin", "2026-12-31")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/abonnements"))
                .andExpect(flash().attribute("erreur", containsString("postérieure")));
    }

    @Test
    @WithMockUser(username = "admin@parking.com", roles = "ADMIN")
    @DisplayName("Un tarif refuse une réduction d'abonné supérieure à 100 %")
    void reductionTarifInvalideRefusee() throws Exception {
        mockMvc.perform(post("/tarifs")
                        .param("zone", "ZONE_INVALIDE")
                        .param("montantParHeure", "2.00")
                        .param("reductionAbonne", "1.01")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tarifs"))
                .andExpect(flash().attribute("erreur", containsString("ne peut pas dépasser 1")));
    }

    @Test
    @WithMockUser(username = "operator@parking.com", roles = "OPERATOR")
    @DisplayName("La borne refuse une immatriculation contenant des caractères interdits")
    void immatriculationBorneInvalideRefusee() throws Exception {
        mockMvc.perform(post("/borne/entree")
                        .param("immatriculation", "CD/AAA")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/borne"))
                .andExpect(flash().attributeExists("erreur"));
    }
}
