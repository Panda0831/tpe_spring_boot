package com.tpe.taxis.security;

import java.util.List;
import java.util.Map;

/**
 * Cœur de la matrice de sécurité : mappe chaque chemin de l'API vers le rôle minimum requis.
 * Utilisé par {@link SecurityConfig} pour construire les règles d'autorisation,
 * et peut servir de référence documentée pour les tests de sécurité.
 *
 * <p>Règles du sujet :</p>
 * <ul>
 *   <li>ADMIN : configuration (tarifs, dispatch) et suppressions ;</li>
 *   <li>MANAGER : gestion métier (courses, flotte, clients, statistiques) ;</li>
 *   <li>OPERATOR : opérations quotidiennes (cycle de vie des courses, dispatch) ;</li>
 *   <li>USER : consultation et commandes de courses.</li>
 * </ul>
 */
public final class SecurityPaths {

    /** Lecture seule : accessible à tout utilisateur authentifié (tous rôles). */
    public static final String[] LECTURE = {
            "/api/chauffeurs/eligible",
            "/api/vehicules/disponibles",
            "/api/stats/**",
            "/api/courses/*/tracking",
            "/api/clients/*/courses"
    };

    /** Actions USER : consultation et commande de course. */
    public static final String[] ACTIONS_USER = {
            "POST /api/courses"   // commander une course
    };

    /** Actions OPERATOR : opérations quotidiennes. */
    public static final String[] ACTIONS_OPERATOR = {
            "POST /api/courses/*/assigner",   // affectation (le dispatcher applique les mêmes règles)
            "POST /api/courses/*/demarrer",
            "POST /api/courses/*/terminer",
            "POST /api/dispatch/**"           // dispatch automatique (attribution des courses)
    };

    /**
     * Actions MANAGER : gestion métier.
     *
     * <p>{@code /**} est utilisé pour les actions PATCH : celles-ci portent toujours sur une
     * sous-ressource d'un identifiant ({@code /api/vehicules/{id}/statut},
     * {@code /api/chauffeurs/{id}/vehicule/{vehiculeId}}…). Un motif {@code /*} ne couvrirait
     * qu'un seul segment et laisserait ces écritures accessibles à tout utilisateur authentifié.</p>
     */
    public static final String[] ACTIONS_MANAGER = {
            "POST /api/chauffeurs", "PUT /api/chauffeurs/*", "PATCH /api/chauffeurs/**",
            "DELETE /api/chauffeurs/*/vehicule",       // retirer un véhicule d'un chauffeur
            "POST /api/vehicules", "PUT /api/vehicules/*", "PATCH /api/vehicules/**",
            "DELETE /api/vehicules/*/chauffeur",       // retirer un chauffeur d'un véhicule
            "POST /api/clients", "PUT /api/clients/*", "PATCH /api/clients/**",
            "POST /api/courses/*/annuler"
    };

    /** Actions ADMIN : configuration et suppressions. */
    public static final String[] ACTIONS_ADMIN = {
            "POST /api/tarifs", "PUT /api/tarifs/*", "DELETE /api/tarifs/*",
            "PUT /api/tarifs/actif", "PATCH /api/tarifs/**",
            "DELETE /api/chauffeurs/*",
            "DELETE /api/vehicules/*",
            "DELETE /api/clients/*",
            "DELETE /api/courses/*"
    };

    /** Matrice « méthode + ant → rôle » lue par SecurityConfig. */
    public static final Map<String, List<String>> REGLES = Map.ofEntries(
            Map.entry("GET", List.of(LECTURE)),
            Map.entry("USER", List.of(ACTIONS_USER)),
            Map.entry("OPERATOR", List.of(ACTIONS_OPERATOR)),
            Map.entry("MANAGER", List.of(ACTIONS_MANAGER)),
            Map.entry("ADMIN", List.of(ACTIONS_ADMIN))
    );

    private SecurityPaths() {
    }
}
