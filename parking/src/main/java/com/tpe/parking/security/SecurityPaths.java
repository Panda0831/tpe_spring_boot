package com.tpe.parking.security;

import java.util.List;

/**
 * Matrice de sécurité du parking : chaque règle « METHODE /chemin » est associée au rôle
 * minimum requis. Consommée par {@link SecurityConfig}.
 *
 * <p>Règles du sujet appliquées au parking :</p>
 * <ul>
 *   <li>ADMIN : configuration (tarifs) et suppressions ;</li>
 *   <li>MANAGER : gestion métier (utilisateurs, véhicules, abonnements) ;</li>
 *   <li>OPERATOR : opérations quotidiennes (entrées/sorties, assignation de places, annulations) ;</li>
 *   <li>USER : consultation du plan, de ses véhicules et de ses réservations, et réservation de place.</li>
 * </ul>
 */
public final class SecurityPaths {

    /** Lecture seule : accessible à tout utilisateur authentifié (tous rôles). */
    public static final String[] LECTURE = {
            "/api/stats/**"
    };

    /** Actions USER : actions limitées. */
    public static final String[] ACTIONS_USER = {
            "POST /api/reservations"          // réserver une place
    };

    /** Actions OPERATOR : opérations quotidiennes. */
    public static final String[] ACTIONS_OPERATOR = {
            "GET /api/stats/**",
            "GET /api/entrees-sorties/**",
            "POST /api/entrees-sorties/**",   // barrières d'entrée / sortie
            "POST /api/places/**",            // assigner / libérer / transférer une place
            "PUT /api/reservations/*",        // modifier une réservation
            "DELETE /api/reservations/*"      // annuler une réservation
    };

    /** Actions MANAGER : gestion métier. */
    public static final String[] ACTIONS_MANAGER = {
            "POST /api/utilisateurs",
            "PUT /api/utilisateurs/*",
            "POST /api/vehicules",
            "PUT /api/vehicules/*",
            "POST /api/abonnements",           // souscrire un abonnement
            "PUT /api/abonnements/*/renouveler"
    };

    /** Actions ADMIN : configuration et suppressions. */
    public static final String[] ACTIONS_ADMIN = {
            "POST /api/tarifs/**",
            "PUT /api/tarifs/**",
            "DELETE /api/tarifs/**",
            "POST /api/places",
            "DELETE /api/utilisateurs/*",
            "DELETE /api/vehicules/*",
            "DELETE /api/abonnements/*",
            "DELETE /api/reservations/*/supprimer"
    };

    private SecurityPaths() {
    }
}
