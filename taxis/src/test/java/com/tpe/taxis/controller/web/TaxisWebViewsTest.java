package com.tpe.taxis.controller.web;

import com.tpe.taxis.entity.Client;
import com.tpe.taxis.entity.Course;
import com.tpe.taxis.entity.CourseStatut;
import com.tpe.taxis.repository.ChauffeurRepository;
import com.tpe.taxis.repository.ClientRepository;
import com.tpe.taxis.repository.CourseRepository;
import com.tpe.taxis.service.CourseService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Interface Web Thymeleaf {@code taxis} : chaque écran se rend avec les données réelles,
 * les filtres et la pagination reposent sur les mêmes requêtes que l'API, et chaque écriture
 * est protégée à la fois par le rôle (matrice de sécurité + {@code @PreAuthorize}) et par le
 * jeton CSRF.
 *
 * <p>La chaîne de filtres Spring Security est appliquée à MockMvc
 * ({@code springSecurity()}) pour que {@code @WithMockUser} et le filtre CSRF se comportent
 * exactement comme en production.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaxisWebViewsTest {

    @TestConfiguration
    static class SecuriteMockMvc {
        @Bean
        MockMvcBuilderCustomizer securiteSpring() {
            return builder -> builder.apply(springSecurity());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ChauffeurRepository chauffeurRepository;

    /** Identifiant de la course EN_ATTENTE issue du jeu de démonstration. */
    private long courseEnAttenteId() {
        return courseService.getCoursesByStatut(CourseStatut.EN_ATTENTE, PageRequest.of(0, 1))
                .getContent().get(0).id();
    }

    /** Ajoute 11 courses en attente pour dépasser la taille d'une page (10). */
    private void creerOnzeCourses() {
        Client client = clientRepository.findAll().get(0);
        for (int i = 0; i < 11; i++) {
            courseRepository.save(Course.builder()
                    .client(client)
                    .pointDepart("Depart test " + i).destination("Arrivee test " + i)
                    .distanceKm(3.0).dateHeure(LocalDateTime.now().minusMinutes(i))
                    .tarif(new BigDecimal("7.90")).commission(new BigDecimal("1.58"))
                    .montantChauffeur(new BigDecimal("6.32"))
                    .statut(CourseStatut.EN_ATTENTE)
                    .build());
        }
    }

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
    @DisplayName("Toute page protégée redirige vers /login sans session")
    void redirectionVersLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @ParameterizedTest
    @CsvSource({
            "/dashboard, dashboard",
            "/courses, courses",
            "/flotte, flotte",
            "/chauffeurs, chauffeurs",
            "/vehicules, vehicules",
            "/clients, clients",
            "/statistiques, statistiques",
            "/tarifs, tarifs"
    })
    @WithMockUser(username = "operator", roles = "OPERATOR")
    @DisplayName("Chaque écran se rend (200) et utilise le bon template")
    void ecransSeRendent(String url, String vue) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(vue));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    @DisplayName("Le tableau de bord affiche les indicateurs, la flotte et les courses à traiter")
    void tableauDeBord() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tableau de bord")))
                .andExpect(content().string(containsString("Courses du jour")))
                .andExpect(content().string(containsString("Chiffre d'affaires du jour")))
                .andExpect(content().string(containsString("Taxis disponibles")))
                .andExpect(content().string(containsString("Courses à traiter")))
                .andExpect(content().string(containsString("Bofenda")));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    @DisplayName("La gestion des courses affiche le formulaire de commande et l'attribution en un clic")
    void coursesEtFiltre() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Créer la course")))
                .andExpect(content().string(containsString("Attribution auto")))
                .andExpect(content().string(containsString("Tous les statuts")))
                .andExpect(content().string(containsString("Sarah Mulumba")));

        // Le filtre par statut applique la même requête que l'API : seules les courses terminées remontent
        mockMvc.perform(get("/courses").param("statut", "TERMINEE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("selected")))
                .andExpect(content().string(containsString("Gare de Kinshasa")))
                .andExpect(content().string(not(containsString("Lingwala"))));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    @DisplayName("La pagination des courses conserve les filtres et découpe bien les pages")
    void paginationDesCourses() throws Exception {
        creerOnzeCourses();

        // 16 courses au total, 10 par page : la page 1 contient les plus récentes, la page 2 les plus anciennes
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Suivant")))
                .andExpect(content().string(containsString("Arrivee test 8")))
                .andExpect(content().string(not(containsString("Arrivee test 10"))));

        mockMvc.perform(get("/courses").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Précédent")))
                .andExpect(content().string(containsString("Arrivee test 10")))
                .andExpect(content().string(not(containsString("Arrivee test 0"))));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("Les chauffeurs affichent l'alerte de permis expiré et se filtrent par recherche")
    void chauffeursEtAlertePermis() throws Exception {
        mockMvc.perform(get("/chauffeurs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("PERM-004-EXPIRE")))
                .andExpect(content().string(containsString("Expiré")))
                .andExpect(content().string(containsString("pastille-SUSPENDU")))
                .andExpect(content().string(containsString("Rechercher")));

        mockMvc.perform(get("/chauffeurs").param("recherche", "kabasele"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Kabasele")))
                .andExpect(content().string(not(containsString("Tshibangu"))));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Les actions de gestion sont masquées pour un USER (pas de formulaire de modification)")
    void actionsMasqueesPourUser() throws Exception {
        mockMvc.perform(get("/chauffeurs"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Mettre à jour"))))
                .andExpect(content().string(not(containsString("Supprimer"))));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("Un MANAGER voit les actions de gestion mais pas la suppression réservée à ADMIN")
    void actionsVisiblesPourManager() throws Exception {
        mockMvc.perform(get("/chauffeurs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mettre à jour")))
                .andExpect(content().string(containsString("Affecter")))
                .andExpect(content().string(not(containsString("Supprimer"))));

        mockMvc.perform(get("/vehicules"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CD-004-GH")))
                .andExpect(content().string(containsString("pastille-MAINTENANCE")))
                .andExpect(content().string(not(containsString("Supprimer"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Un ADMIN voit la suppression sur les fiches chauffeurs et véhicules")
    void actionsVisiblesPourAdmin() throws Exception {
        mockMvc.perform(get("/chauffeurs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Supprimer")));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    @DisplayName("La disponibilité de la flotte regroupe chauffeurs et véhicules par statut")
    void disponibiliteDeLaFlotte() throws Exception {
        mockMvc.perform(get("/flotte"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Disponibilité de la flotte")))
                .andExpect(content().string(containsString("pastille-DISPONIBLE")))
                .andExpect(content().string(containsString("pastille-MAINTENANCE")))
                .andExpect(content().string(containsString("pastille-SUSPENDU")))
                .andExpect(content().string(containsString("CD-005-IJ")));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("Les statistiques affichent le CA, les commissions et l'histogramme journalier")
    void statistiques() throws Exception {
        mockMvc.perform(get("/statistiques").param("dateDebut", "2026-09-01").param("dateFin", "2026-10-10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Rapports financiers et statistiques")))
                .andExpect(content().string(containsString("Commission société")))
                .andExpect(content().string(containsString("Taux d'annulation")))
                .andExpect(content().string(containsString("barre-revenu")))
                .andExpect(content().string(containsString("2026-09-01")));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    @DisplayName("Un formulaire sans jeton CSRF est refusé")
    void csrfObligatoire() throws Exception {
        mockMvc.perform(post("/courses")
                        .param("clientId", "1")
                        .param("pointDepart", "Gombe")
                        .param("destination", "Limete")
                        .param("distanceKm", "4"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Un USER peut commander une course depuis l'interface")
    void userCommandeUneCourse() throws Exception {
        mockMvc.perform(post("/courses")
                        .param("clientId", "1")
                        .param("pointDepart", "Gombe, Avenue Kasa-Vubu")
                        .param("destination", "Lingwala, marché Gambela")
                        .param("distanceKm", "6.2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attribute("succes", containsString("créée")));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Un USER ne peut pas dispatcher une course (403) et voit la page d'erreur")
    void userNePeutPasDispatcher() throws Exception {
        mockMvc.perform(post("/courses/" + courseEnAttenteId() + "/dispatcher").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Accès refusé")));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    @DisplayName("Un OPERATOR dispatche une course en attente en un clic")
    void operatorDispatcheUneCourse() throws Exception {
        mockMvc.perform(post("/courses/" + courseEnAttenteId() + "/dispatcher").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attribute("succes", containsString("attribuée")));
    }

    @Test
    @WithMockUser(username = "operator", roles = "OPERATOR")
    @DisplayName("L'annulation d'une course est réservée à MANAGER+ (403 pour OPERATOR)")
    void operatorNePeutPasAnnuler() throws Exception {
        mockMvc.perform(post("/courses/" + courseEnAttenteId() + "/annuler").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("Un MANAGER peut annuler une course et en commander une nouvelle")
    void managerAnnuleEtCree() throws Exception {
        long courseId = courseEnAttenteId();

        mockMvc.perform(post("/courses/" + courseId + "/annuler").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"))
                .andExpect(flash().attribute("succes", containsString("annulée")));

        mockMvc.perform(post("/courses")
                        .param("clientId", "2")
                        .param("pointDepart", "Gombe")
                        .param("destination", "Ngaliema")
                        .param("distanceKm", "5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("succes", containsString("créée")));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Un USER ne peut pas créer de chauffeur (403)")
    void userNePeutPasCreerUnChauffeur() throws Exception {
        mockMvc.perform(post("/chauffeurs")
                        .param("nom", "Test").param("prenom", "User")
                        .param("telephone", "+243970000901").param("numeroPermis", "PERM-WEB-USER")
                        .param("dateExpirationPermis", "2030-01-01")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("Un MANAGER crée et modifie un chauffeur depuis l'interface")
    void managerGereUnChauffeur() throws Exception {
        mockMvc.perform(post("/chauffeurs")
                        .param("nom", "Mputu").param("prenom", "Célestin")
                        .param("telephone", "+243970000902").param("numeroPermis", "PERM-WEB-1")
                        .param("dateExpirationPermis", "2030-01-01")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/chauffeurs"))
                .andExpect(flash().attribute("succes", containsString("enregistré")));

        long chauffeurId = chauffeurRepository.findByNumeroPermis("PERM-WEB-1").orElseThrow().getId();

        mockMvc.perform(post("/chauffeurs/" + chauffeurId + "/statut")
                        .param("statut", "EN_PAUSE").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("succes", containsString("EN_PAUSE")));

        // La suppression d'un chauffeur est réservée à ADMIN
        mockMvc.perform(post("/chauffeurs/" + chauffeurId + "/supprimer").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Un ADMIN supprime un chauffeur depuis l'interface")
    void adminSupprimeUnChauffeur() throws Exception {
        mockMvc.perform(post("/chauffeurs")
                        .param("nom", "Bemba").param("prenom", "Odette")
                        .param("telephone", "+243970000903").param("numeroPermis", "PERM-WEB-2")
                        .param("dateExpirationPermis", "2030-01-01")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        long chauffeurId = chauffeurRepository.findByNumeroPermis("PERM-WEB-2").orElseThrow().getId();

        mockMvc.perform(post("/chauffeurs/" + chauffeurId + "/supprimer").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/chauffeurs"))
                .andExpect(flash().attribute("succes", containsString("supprimé")));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("Un MANAGER crée et modifie un client depuis l'interface web")
    void managerGereUnClient() throws Exception {
        mockMvc.perform(post("/clients")
                        .param("nom", "Kabongo").param("prenom", "Sylvie")
                        .param("telephone", "+243899999001")
                        .param("email", "sylvie.kabongo@test.cd")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"))
                .andExpect(flash().attribute("succes", containsString("enregistré")));

        long clientId = clientRepository.findByTelephone("+243899999001").orElseThrow().getId();

        mockMvc.perform(post("/clients/" + clientId)
                        .param("nom", "Kabongo-Modifie")
                        .param("prenom", "Sylvie")
                        .param("telephone", "+243899999001")
                        .param("email", "sylvie.k@test.cd")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"))
                .andExpect(flash().attribute("succes", containsString("mis à jour")));

        // Seul ADMIN peut supprimer
        mockMvc.perform(post("/clients/" + clientId + "/supprimer").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("Un numéro de téléphone invalide est refusé à la création et à la modification d'un client")
    void telephoneClientInvalideRefuse() throws Exception {
        mockMvc.perform(post("/clients")
                        .param("nom", "Test")
                        .param("telephone", "telephone-invalide")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erreur", "Format de numéro de téléphone invalide"));

        org.junit.jupiter.api.Assertions.assertFalse(clientRepository.existsByTelephone("telephone-invalide"));

        Client client = clientRepository.findAll().get(0);
        String telephoneInitial = client.getTelephone();
        mockMvc.perform(post("/clients/" + client.getId())
                        .param("telephone", "telephone-invalide")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erreur", "Format de numéro de téléphone invalide"));

        org.junit.jupiter.api.Assertions.assertEquals(
                telephoneInitial, clientRepository.findById(client.getId()).orElseThrow().getTelephone());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("Un téléphone invalide bloque aussi la création d'un chauffeur")
    void telephoneChauffeurInvalideRefuse() throws Exception {
        mockMvc.perform(post("/chauffeurs")
                        .param("nom", "Mbuyi")
                        .param("prenom", "Aline")
                        .param("telephone", "pas-un-numero")
                        .param("numeroPermis", "PERM-WEB-INVALID")
                        .param("dateExpirationPermis", "2030-01-01")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erreur", "Format de numéro de téléphone invalide"));

        org.junit.jupiter.api.Assertions.assertTrue(
                chauffeurRepository.findByNumeroPermis("PERM-WEB-INVALID").isEmpty());

        var chauffeur = chauffeurRepository.findAll().get(0);
        String telephoneInitial = chauffeur.getTelephone();
        mockMvc.perform(post("/chauffeurs/" + chauffeur.getId())
                        .param("telephone", "pas-un-numero")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erreur", "Format de numéro de téléphone invalide"));
        org.junit.jupiter.api.Assertions.assertEquals(telephoneInitial,
                chauffeurRepository.findById(chauffeur.getId()).orElseThrow().getTelephone());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("La validation bloque une course dont la distance n'est pas positive")
    void distanceCourseInvalideRefusee() throws Exception {
        mockMvc.perform(post("/courses")
                        .param("clientId", "1")
                        .param("pointDepart", "Gombe")
                        .param("destination", "Limete")
                        .param("distanceKm", "0")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erreur", "La distance doit être strictement positive"));

        org.junit.jupiter.api.Assertions.assertEquals(1,
                courseService.getCoursesByStatut(CourseStatut.EN_ATTENTE, PageRequest.of(0, 10))
                        .getTotalElements());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("Les validations s'appliquent aux formulaires véhicule de création et de modification")
    void validationsVehiculeCreationEtModification() throws Exception {
        mockMvc.perform(post("/vehicules")
                        .param("immatriculation", "CD-INVALID")
                        .param("marque", "Test")
                        .param("modele", "Test")
                        .param("capacite", "0")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erreur", "La capacité minimale est de 1 passager"));

        mockMvc.perform(post("/vehicules/1")
                        .param("kilometrage", "-1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erreur", "Le kilométrage doit être positif ou nul"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Un pourcentage de commission supérieur à 100 est refusé par le formulaire tarifaire")
    void commissionTarifInvalideRefusee() throws Exception {
        mockMvc.perform(post("/tarifs")
                        .param("id", "1")
                        .param("tarifBase", "3.00")
                        .param("prixKm", "2.00")
                        .param("commissionPourcentage", "101")
                        .param("poidsProximite", "0.70")
                        .param("poidsEquite", "0.30")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erreur",
                        "Le pourcentage de commission ne peut pas dépasser 100%"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Un ADMIN met à jour les tarifs et les coefficients de dispatch depuis l'interface web")
    void adminModifieTarifs() throws Exception {
        mockMvc.perform(post("/tarifs")
                        .param("id", "1")
                        .param("tarifBase", "3.00")
                        .param("prixKm", "2.00")
                        .param("commissionPourcentage", "25.0")
                        .param("poidsProximite", "0.70")
                        .param("poidsEquite", "0.30")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tarifs"))
                .andExpect(flash().attribute("succes", containsString("mis à jour")));
    }
}
