package com.tpe.parking.controller.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API REST : codes HTTP stricts, exposition de DTOs, pagination/tri/filtres,
 * validation des requêtes et refus 403 selon le rôle (matrice {@code SecurityPaths}).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ParkingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ADMIN = "admin@parking.com";
    private static final String MANAGER = "manager@parking.com";
    private static final String OPERATOR = "operator@parking.com";
    private static final String USER = "user@parking.com";
    private static final String MOT_DE_PASSE = "password";

    @Test
    @DisplayName("L'API exige une authentification (401 en JSON)")
    void apiNonAuthentifiee() throws Exception {
        mockMvc.perform(get("/api/places"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Liste des places paginée, triée et filtrée, sans entité JPA")
    void placesPagineesEtFiltrees() throws Exception {
        mockMvc.perform(get("/api/places")
                        .param("page", "0").param("size", "5")
                        .param("statut", "LIBRE").param("sort", "numero,asc")
                        .with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(22))
                .andExpect(jsonPath("$.content[0].numero").exists());

        mockMvc.perform(get("/api/places")
                        .param("zone", "Etage1").param("type", "VIP")
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].numero").value("B1"));
    }

    @Test
    @DisplayName("Seul ADMIN peut créer une place via l'API")
    void creationPlaceApiProtegee() throws Exception {
        String corpsValide = """
                {"numero":"c-25","zone":"RDC","type":"STANDARD"}
                """;

        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON).content(corpsValide)
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON).content(corpsValide)
                        .with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("C-25"))
                .andExpect(jsonPath("$.statut").value("LIBRE"));

        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":"C-25","zone":"Etage1","type":"VIP"}
                                """)
                        .with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":"C 26","zone":"RDC","type":"STANDARD"}
                                """)
                        .with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Réservations : création 201, validation 400, conflit 409, ressource absente 404")
    void reservationsCasNominalEtErreurs() throws Exception {
        String corpsValide = """
                {"utilisateurId":4,"vehiculeId":2,"placeId":9,
                 "dateArriveePrevue":"2027-05-10T10:00:00","dateDepartPrevue":"2027-05-10T12:00:00"}
                """;

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(corpsValide)
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"utilisateurId":4,"vehiculeId":2,
                                 "dateArriveePrevue":"2027-05-10T10:00:00","dateDepartPrevue":"2027-05-10T12:00:00"}
                                """)
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"utilisateurId":4,"vehiculeId":2}
                                """)
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());

        // Deux réservations chevauchantes sur la même place => 409 Conflict
        String chevauchement = """
                {"utilisateurId":5,"vehiculeId":3,"placeId":9,
                 "dateArriveePrevue":"2027-05-10T11:00:00","dateDepartPrevue":"2027-05-10T13:00:00"}
                """;
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(chevauchement)
                        .with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/reservations/999999")
                        .with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("USER ne voit que ses réservations et véhicules et ne peut usurper un propriétaire")
    void donneesUtilisateurLimiteesAProprietaire() throws Exception {
        mockMvc.perform(get("/api/reservations").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].utilisateurId").value(4));

        mockMvc.perform(get("/api/reservations").param("utilisateurId", "5")
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reservations/utilisateur/5")
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/vehicules").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].utilisateurId").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is(4))));
        mockMvc.perform(get("/api/vehicules/3").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/utilisateurs").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"utilisateurId":5,"vehiculeId":3,"placeId":10,
                                 "dateArriveePrevue":"2027-08-10T10:00:00",
                                 "dateDepartPrevue":"2027-08-10T12:00:00"}
                                """)
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reservations").with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].utilisateurId").value(4));
        mockMvc.perform(get("/api/utilisateurs").with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    @DisplayName("Entrées/sorties : entrée 201 (place occupée) puis sortie 200 facturée")
    void cycleEntreeSortie() throws Exception {
        mockMvc.perform(get("/api/entrees-sorties").param("enCours", "true")
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(post("/api/entrees-sorties/entree")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"immatriculation\":\"CD-AAA-002\"}")
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroPlace").exists())
                .andExpect(jsonPath("$.enCours").value(true));

        mockMvc.perform(get("/api/places").param("statut", "OCCUPEE")
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(post("/api/entrees-sorties/sortie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"immatriculation\":\"CD-AAA-002\"}")
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montant").exists())
                .andExpect(jsonPath("$.enCours").value(false));
    }

    @Test
    @DisplayName("Statistiques : occupation, zones, revenus et véhicules présents")
    void statistiques() throws Exception {
        mockMvc.perform(get("/api/stats/occupation").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlaces").value(24))
                .andExpect(jsonPath("$.placesLibres").value(22));

        mockMvc.perform(get("/api/stats/occupation/zones").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/stats/revenus").param("jours", "7").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)));

        mockMvc.perform(get("/api/stats/vehicules-presents").with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        mockMvc.perform(get("/api/stats/occupation").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tarifs : DTOs en lecture, écritures réservées à ADMIN (403 sinon)")
    void tarifsEtRoles() throws Exception {
        mockMvc.perform(get("/api/tarifs").with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].zone").exists())
                .andExpect(jsonPath("$[0].montantParHeure").exists());

        String nouveauTarif = """
                {"zone":"TOIT","montantParHeure":3.50,"tarifMinimum":2.00,
                 "forfaitJournalier":20.00,"reductionAbonne":0.10,"majorationApres":0.50}
                """;

        mockMvc.perform(post("/api/tarifs").contentType(MediaType.APPLICATION_JSON).content(nouveauTarif)
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/tarifs").contentType(MediaType.APPLICATION_JSON).content(nouveauTarif)
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/tarifs").contentType(MediaType.APPLICATION_JSON).content(nouveauTarif)
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/tarifs").contentType(MediaType.APPLICATION_JSON).content(nouveauTarif)
                        .with(httpBasic(ADMIN, MOT_DE_PASSE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.zone").value("TOIT"));
    }

    @Test
    @DisplayName("Hiérarchie : OPERATOR peut gérer les places, USER non")
    void hierarchieDesRoles() throws Exception {
        mockMvc.perform(post("/api/places/assigner")
                        .param("utilisateurId", "4").param("vehiculeId", "4")
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/places/assigner")
                        .param("utilisateurId", "4").param("vehiculeId", "4")
                        .with(httpBasic(OPERATOR, MOT_DE_PASSE)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/abonnements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"utilisateurId":4,"vehiculeId":4,"type":"MENSUEL",
                                 "dateDebut":"2027-01-01","dateFin":"2027-02-01"}
                                """)
                        .with(httpBasic(USER, MOT_DE_PASSE)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Recherche : utilisateurs par nom, véhicules par marque")
    void rechercheEtFiltres() throws Exception {
        mockMvc.perform(get("/api/utilisateurs").param("recherche", "kasongo")
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email", containsString("user@parking.com")));

        mockMvc.perform(get("/api/vehicules").param("recherche", "toyota")
                        .with(httpBasic(MANAGER, MOT_DE_PASSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
