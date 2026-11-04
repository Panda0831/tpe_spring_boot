package com.tpe.taxis.controller.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API REST {@code taxis} : codes HTTP stricts, pagination/filtres, moteur de dispatch
 * (attribution automatique et manuelle, blocages métier) et refus 403 selon la matrice
 * {@code SecurityPaths} (ADMIN &gt; MANAGER &gt; OPERATOR &gt; USER).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaxisApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ADMIN = "admin";
    private static final String MANAGER = "manager";
    private static final String OPERATOR = "operator";
    private static final String USER = "user";
    private static final String MOT_DE_PASSE = "password";

    /** Corps JSON générique de commande de course. */
    private static String corpsCourse(Long clientId, String depart, String destination, double distanceKm) {
        return """
                {"clientId":%d,"pointDepart":"%s","destination":"%s","distanceKm":%s}
                """.formatted(clientId, depart, destination, distanceKm);
    }

    /** Crée une course EN_ATTENTE par l'API (rôle USER) et retourne son identifiant. */
    private long creerCourse() throws Exception {
        String reponse = mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsCourse(1L, "Départ test", "Arrivée test", 4.0))
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(reponse, "$.id")).longValue();
    }

    @Test
    @DisplayName("L'API exige une authentification (401 en JSON)")
    void apiNonAuthentifiee() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Courses paginées, triées et filtrées, sans entité JPA exposée")
    void coursesPagineesEtFiltrees() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .param("page", "0").param("size", "2").param("sort", "dateHeure,desc")
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.content[0].nomClient").exists());

        mockMvc.perform(get("/api/courses/statut/EN_ATTENTE").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].statut").value("EN_ATTENTE"));

        // Client 1 : une course terminée hier + une course annulée
        mockMvc.perform(get("/api/courses/client/1").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        LocalDateTime debut = LocalDate.now().minusDays(2).atStartOfDay();
        LocalDateTime fin = LocalDate.now().plusDays(1).atStartOfDay();
        DateTimeFormatter iso = DateTimeFormatter.ISO_DATE_TIME;
        mockMvc.perform(get("/api/courses/periode")
                        .param("debut", debut.format(iso)).param("fin", fin.format(iso))
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Commande de course : 201 par USER, validation 400, client inconnu 404")
    void creationCourseEtValidation() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsCourse(1L, "Gombe", "Limete", 6.0))
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.chauffeurId", nullValue()))
                .andExpect(jsonPath("$.tarif").value(13.30));   // 2.50 + 1.80 x 6

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":1,"pointDepart":"Gombe"}
                                """)
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.destination").exists())
                .andExpect(jsonPath("$.validationErrors.distanceKm").exists());

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsCourse(1L, "Gombe", "Limete", 0.0))
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsCourse(999999L, "Gombe", "Limete", 3.0))
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/courses/999999").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Dispatch automatique : OPERATOR affecte un chauffeur éligible avec véhicule hors maintenance")
    void dispatchAutomatique() throws Exception {
        mockMvc.perform(post("/api/dispatch/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsCourse(1L, "Gombe", "Limete", 5.0))
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/dispatch/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpsCourse(1L, "Gombe", "Limete", 5.0))
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("ASSIGNEE"))
                // Les 3 chauffeurs éligibles sont les id 1, 2 et 3 : ni permis expiré (4) ni suspendu (5)
                .andExpect(jsonPath("$.chauffeurId", oneOf(1, 2, 3)))
                .andExpect(jsonPath("$.immatriculationVehicule", not("CD-004-GH")))
                .andExpect(jsonPath("$.nomChauffeur").isNotEmpty())
                .andExpect(jsonPath("$.montantChauffeur").exists());
    }

    @Test
    @DisplayName("Affectation manuelle : chauffeur éligible affecté, permis expiré / suspendu / véhicule en maintenance refusés")
    void affectationManuelleEtBlocages() throws Exception {
        long course = creerCourse();

        mockMvc.perform(post("/api/courses/" + course + "/assigner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chauffeurId\":2,\"vehiculeId\":2}")
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/courses/" + course + "/assigner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chauffeurId\":2}")   // véhicule du chauffeur repris automatiquement
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ASSIGNEE"))
                .andExpect(jsonPath("$.chauffeurId").value(2))
                .andExpect(jsonPath("$.immatriculationVehicule").value("CD-002-CD"));

        // Chauffeur 4 : permis expiré (statut DISPONIBLE mais permis invalide)
        long expire = creerCourse();
        mockMvc.perform(post("/api/courses/" + expire + "/assigner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chauffeurId\":4}")
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CHAUFFEUR_INDISPONIBLE"));

        // Chauffeur 5 : suspendu
        long suspendu = creerCourse();
        mockMvc.perform(post("/api/courses/" + suspendu + "/assigner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chauffeurId\":5}")
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isBadRequest());

        // Véhicule 4 : en maintenance
        long maintenance = creerCourse();
        mockMvc.perform(post("/api/courses/" + maintenance + "/assigner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chauffeurId\":1,\"vehiculeId\":4}")
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VEHICULE_INDISPONIBLE"));

        // Chauffeur 2 déjà sur une course active => conflit
        long conflit = creerCourse();
        mockMvc.perform(post("/api/courses/" + conflit + "/assigner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chauffeurId\":2}")
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    @DisplayName("Les écritures sur les sous-ressources (statuts, affectations) sont réservées à MANAGER+")
    void sousRessourcesReserveesAuxGestionnaires() throws Exception {
        // PATCH /api/chauffeurs/{id}/statut
        mockMvc.perform(patch("/api/chauffeurs/2/statut").param("statut", "EN_PAUSE")
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/chauffeurs/2/statut").param("statut", "EN_PAUSE")
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_PAUSE"));

        // PATCH /api/chauffeurs/{id}/vehicule/{vehiculeId}
        mockMvc.perform(patch("/api/chauffeurs/1/vehicule/5").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());

        // PATCH /api/vehicules/{id}/statut
        mockMvc.perform(patch("/api/vehicules/2/statut").param("statut", "HORS_SERVICE")
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());

        // PATCH /api/vehicules/{id}/chauffeur/{chauffeurId}
        mockMvc.perform(patch("/api/vehicules/5/chauffeur/3").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/vehicules/5/chauffeur/3").with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chauffeurId").value(3));
    }

    @Test
    @DisplayName("Cycle de vie : ASSIGNEE -> EN_COURS -> TERMINEE, et annulation réservée à MANAGER+")
    void cycleDeVieEtAnnulation() throws Exception {
        long course = creerCourse();

        mockMvc.perform(post("/api/courses/" + course + "/assigner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chauffeurId\":1}")
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/courses/" + course + "/demarrer").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_COURS"));

        mockMvc.perform(post("/api/courses/" + course + "/terminer").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINEE"));

        // Une course terminée ne peut plus démarrer
        mockMvc.perform(post("/api/courses/" + course + "/demarrer").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isConflict());

        long aAnnuler = creerCourse();
        mockMvc.perform(post("/api/courses/" + aAnnuler + "/annuler").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/courses/" + aAnnuler + "/annuler").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/courses/" + aAnnuler + "/annuler").with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"));

        // Suppression d'une course : ADMIN uniquement
        long aSupprimer = creerCourse();
        mockMvc.perform(delete("/api/courses/" + aSupprimer).with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/courses/" + aSupprimer).with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Paramètres de tarif : lecture authentifiée, écritures réservées à ADMIN")
    void tarifsReservesAdmin() throws Exception {
        mockMvc.perform(get("/api/tarifs").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tarifBase").exists())
                .andExpect(jsonPath("$[0].poidsProximite").exists());

        String nouveau = """
                {"tarifBase":3.00,"prixKm":2.00,"commissionPourcentage":25.00,
                 "poidsProximite":0.70,"poidsEquite":0.30}
                """;

        mockMvc.perform(post("/api/tarifs").contentType(MediaType.APPLICATION_JSON).content(nouveau)
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/tarifs").contentType(MediaType.APPLICATION_JSON).content(nouveau)
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/tarifs").contentType(MediaType.APPLICATION_JSON).content(nouveau)
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());

        String reponse = mockMvc.perform(post("/api/tarifs")
                        .contentType(MediaType.APPLICATION_JSON).content(nouveau)
                        .with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tarifBase").value(3.00))
                .andExpect(jsonPath("$.poidsEquite").value(0.30))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) com.jayway.jsonpath.JsonPath.read(reponse, "$.id")).longValue();

        mockMvc.perform(delete("/api/tarifs/" + id).with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/tarifs/" + id).with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Flotte : gestion MANAGER (chauffeurs, véhicules, clients) et suppressions ADMIN")
    void gestionFlotteEtHierarchie() throws Exception {
        String nouveauChauffeur = """
                {"nom":"Mukendi","prenom":"Alice","telephone":"+243970000900",
                 "numeroPermis":"PERM-TEST-9","dateExpirationPermis":"2030-01-01"}
                """;

        mockMvc.perform(post("/api/chauffeurs").contentType(MediaType.APPLICATION_JSON).content(nouveauChauffeur)
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/chauffeurs").contentType(MediaType.APPLICATION_JSON).content(nouveauChauffeur)
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());

        String reponse = mockMvc.perform(post("/api/chauffeurs")
                        .contentType(MediaType.APPLICATION_JSON).content(nouveauChauffeur)
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomComplet").value("Alice Mukendi"))
                .andExpect(jsonPath("$.eligiblePourCourse").value(true))
                .andReturn().getResponse().getContentAsString();
        long chauffeurId = ((Number) com.jayway.jsonpath.JsonPath.read(reponse, "$.id")).longValue();

        mockMvc.perform(patch("/api/chauffeurs/" + chauffeurId + "/statut").param("statut", "EN_PAUSE")
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_PAUSE"))
                .andExpect(jsonPath("$.eligiblePourCourse").value(false));

        mockMvc.perform(delete("/api/chauffeurs/" + chauffeurId).with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/chauffeurs/" + chauffeurId).with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isNoContent());

        // Détacher / rattacher véhicule et chauffeur : MANAGER
        mockMvc.perform(delete("/api/chauffeurs/1/vehicule").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/vehicules/1/chauffeur").with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chauffeurId", nullValue()));
        mockMvc.perform(patch("/api/vehicules/1/chauffeur/1").with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chauffeurId").value(1));

        // Clients : écriture MANAGER+, lecture ouverte aux utilisateurs authentifiés
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Nkulu","prenom":"Esther","telephone":"+243980000900"}
                                """)
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Nkulu","prenom":"Esther","telephone":"+243980000900"}
                                """)
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomComplet").value("Esther Nkulu"));

        mockMvc.perform(get("/api/clients").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    @DisplayName("Flotte : véhicule en maintenance exclu du dispatch, statuts et indisponibilités")
    void etatDeLaFlotte() throws Exception {
        mockMvc.perform(get("/api/chauffeurs/eligibles").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/api/vehicules/disponibles").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        mockMvc.perform(get("/api/vehicules/statut/MAINTENANCE").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].immatriculation").value("CD-004-GH"));

        // Un véhicule en maintenance ne peut pas être affecté à une nouvelle course
        mockMvc.perform(patch("/api/vehicules/5/statut").param("statut", "MAINTENANCE")
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("MAINTENANCE"));

        mockMvc.perform(patch("/api/vehicules/5/statut").param("statut", "DISPONIBLE")
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Statistiques d'exploitation : CA de la journée d'hier, commissions et activité")
    void statistiques() throws Exception {
        LocalDateTime debut = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime fin = LocalDate.now().atStartOfDay();
        DateTimeFormatter iso = DateTimeFormatter.ISO_DATE_TIME;

        mockMvc.perform(get("/api/stats/revenue")
                        .param("dateDebut", debut.format(iso)).param("dateFin", fin.format(iso))
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                // Les 3 courses terminées d'hier : 16.00 + 60.10 + 24.10 = 100.20
                .andExpect(jsonPath("$.nombreCoursesTerminees").value(3))
                .andExpect(jsonPath("$.chiffreAffairesTotal").value(100.20))
                .andExpect(jsonPath("$.caParChauffeur", hasSize(3)))
                .andExpect(jsonPath("$.caParVehicule", hasSize(3)));

        mockMvc.perform(get("/api/stats/activity").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCourses").value(greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.tauxAnnulation").isNumber())
                .andExpect(jsonPath("$.distanceTotaleKm").isNumber())
                .andExpect(jsonPath("$.chauffeurPlusActif").isNotEmpty());
    }

    @Test
    @DisplayName("Recherche et filtres : chauffeurs, véhicules, clients")
    void rechercheEtFiltres() throws Exception {
        mockMvc.perform(get("/api/chauffeurs/search").param("nom", "kabasele").param("prenom", "kabasele")
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].numeroPermis").value("PERM-001"));

        mockMvc.perform(get("/api/chauffeurs").param("page", "0").param("size", "2")
                        .param("sort", "nom,asc").with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5));

        mockMvc.perform(get("/api/vehicules/search").param("marque", "toyota").param("modele", "toyota")
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/clients/search").param("q", "mulumba")
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email", containsString("sarah.mulumba")));

        mockMvc.perform(get("/api/chauffeurs/permis/PERM-005").with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("SUSPENDU"));

        mockMvc.perform(get("/api/chauffeurs/permis/PERM-INEXISTANT").with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/chauffeurs").param("sort", "colonneInexistante")
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isBadRequest());
    }
}
