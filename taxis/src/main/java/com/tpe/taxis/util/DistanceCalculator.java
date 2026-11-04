package com.tpe.taxis.util;

import java.util.Locale;

/**
 * Outils de calcul de distance pour le moteur d'attribution des courses.
 *
 * <p>L'application ne stocke pas de coordonnées GPS : les adresses sont des libellés libres.
 * {@link #estimerDistanceKm(String, String)} fournit donc une <strong>estimation déterministe</strong>
 * (même couple d'adresses =&gt; même distance) obtenue par projection des adresses sur une grille
 * pseudo-géographique. Elle peut être remplacée sans changement d'interface par un vrai service
 * de géocodage, et {@link #haversineKm(double, double, double, double)} est déjà disponible
 * pour travailler avec de vraies coordonnées.</p>
 */
public final class DistanceCalculator {

    /** Côté de la grille pseudo-géographique, en kilomètres (50 km x 50 km). */
    private static final double TAILLE_GRILLE_KM = 50.0;

    /** Nombre de cellules par axe (précision de 0,1 km). */
    private static final int CELLULES_PAR_AXE = (int) (TAILLE_GRILLE_KM * 10);

    /** Rayon moyen de la Terre, en kilomètres. */
    private static final double RAYON_TERRE_KM = 6371.0;

    private DistanceCalculator() {
    }

    /**
     * Distance orthodromique (formule de haversine) entre deux points GPS, en kilomètres.
     *
     * @param lat1 latitude du point A, en degrés
     * @param lon1 longitude du point A, en degrés
     * @param lat2 latitude du point B, en degrés
     * @param lon2 longitude du point B, en degrés
     * @return distance en kilomètres, toujours positive
     */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(deltaLon / 2), 2);
        return 2 * RAYON_TERRE_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    /**
     * Estimation déterministe de la distance (en kilomètres) entre deux adresses libres.
     *
     * <p>Deux adresses identiques (hors casse et espaces multiples) donnent 0 km ;
     * deux adresses distinctes donnent une distance stable comprise entre 0 et ~70 km.</p>
     *
     * @param adresseA première adresse (ex : dernière destination connue du chauffeur)
     * @param adresseB seconde adresse (ex : point de départ de la course à affecter)
     * @return distance estimée en kilomètres, toujours positive
     */
    public static double estimerDistanceKm(String adresseA, String adresseB) {
        if (adresseA == null || adresseB == null) {
            return 0.0;
        }
        String a = normaliser(adresseA);
        String b = normaliser(adresseB);
        if (a.isEmpty() || b.isEmpty() || a.equals(b)) {
            return 0.0;
        }
        double[] grilleA = projeterSurGrille(a);
        double[] grilleB = projeterSurGrille(b);
        double dx = grilleA[0] - grilleB[0];
        double dy = grilleA[1] - grilleB[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static String normaliser(String adresse) {
        return adresse.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    /**
     * Projette une adresse normalisée sur une grille de {@link #TAILLE_GRILLE_KM} km de côté.
     * Le hash de {@code String} étant spécifié par la JLS, la projection est stable
     * d'une exécution à l'autre et d'une JVM à l'autre.
     */
    private static double[] projeterSurGrille(String adresseNormalisee) {
        int hash = adresseNormalisee.hashCode();
        double x = Math.floorMod(hash, CELLULES_PAR_AXE) / 10.0;
        double y = Math.floorMod(hash / CELLULES_PAR_AXE, CELLULES_PAR_AXE) / 10.0;
        return new double[]{x, y};
    }
}
