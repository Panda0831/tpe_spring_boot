# 📋 Plan d'Action & TODO List — Projets Framework Java (TPE)

> **Projets retenus** :
> 1. **Projet 4** : Système de gestion d'un parking privé (`/parking`)
> 2. **Projet 3** : Application de gestion de flotte de taxis (`/taxis`)
> 
> **Socle technique commun** : Java 17+, Spring Boot, Spring Data JPA, Spring Security, Thymeleaf, PostgreSQL / H2 (tests), Lombok, Bean Validation.

---

## 🔍 Synthèse de l'Audit Initial

| Composant / Projet | Parking (`/parking`) | Taxis (`/taxis`) | Exigences Communes Sujet |
| :--- | :--- | :--- | :--- |
| **Compilation Maven** | ✅ **Succès** (corrigé) | ⚠️ Compile mais vide | Doit compiler sans avertissement ni erreur |
| **Entités JPA** | ✅ **Complètes** (relations, rôles, contraintes) | 🔴 Aucune entité | Entités complètes, relations & contraintes |
| **Architecture DTO** | ✅ **Conforme** (`request`/`response` étanches) | 🔴 Vide | DTOs Request/Response étanches, entités interdites en REST |
| **Mappers** | ✅ **Complets** (`TarifMapper` réparé, mappers DTO) | 🔴 Aucun | Mappage complet Entité <-> DTO |
| **Repositories** | ✅ **Complets** (`AbonnementRepository` ajouté) | 🔴 Aucun | Repositories JPA + requêtes dérivées & Pageable |
| **Couche Service** | ✅ **Complète** (tous les services implémentés, compilation validée) | ✅ **Complète** (6 services + dispatcher, compilation validée) | Services métier transactionnels réutilisés par REST & Web |
| **Contrôleurs REST** | ✅ **Complets** (VehiculeRestController, UtilisateurRestController, AbonnementRestController, ReservationRestController, TarifRestController, PlaceRestController, EntreeSortieRestController, ParkingStatsRestController) | ✅ **Complets** (7 contrôleurs : chauffeurs, véhicules, clients, courses, dispatch, tarifs, stats) | API REST HTTP conforme, validation DTO, codes HTTP stricts |
| **Interface Web MVC** | 🟢 **Complète** (6 écrans + login + page d'erreur, CSRF actif) | 🔴 **Vide** | Thymeleaf + Bootstrap, dashboards visuels, messages flash |
| **Sécurité (Security)**| 🔴 Non configurée | 🔴 Non configurée | 4 Rôles (ADMIN, MANAGER, OPERATOR, USER) + Form & REST Auth |
| **Gestion Exceptions** | ✅ **Complète** (Exceptions métier + ErrorResponse + GlobalExceptionHandler créés) | 🔴 Aucune | Exceptions métier + `@RestControllerAdvice` centralisé |
| **Pagination / Filtres**| 🟢 REST + Web (`Pageable`/`Page`, filtres, recherche) | ✅ Côté REST (Pageable + filtres statut/client/période/recherche) | Pageable, sort, filtres sur listes importantes |
| **Données & Tests** | 🟢 Seed de démo + 48 tests (H2) + script 85/85 | 🔴 Tests vides | Profil de test H2, DataInitializer de démo |

---

## 🐞 Correctifs Critiques (parking, session en cours)

1. **Matrice de rôles inopérante** — `SecurityConfig.antVersRegex()` produisait un `//` initial
   (`^//\Qapi\E/...`) : aucune règle de `SecurityPaths` ne matchait, donc **tout utilisateur authentifié
   pouvait tout faire** (un USER pouvait supprimer un tarif). Corrigé (conversion segment par segment +
   `/**` couvrant aussi le chemin parent) et complété par les règles manquantes
   (`POST /api/abonnements` MANAGER, `PUT /api/reservations/*` OPERATOR).
   ⚠️ **Le projet `taxis` contient le même bug — à corriger à l'Étape 7.**
2. **Double facturation des dépassements** — `calculerMontantAvecDepassement()` facturait la durée réelle
   **puis** ajoutait les heures supplémentaires. Corrigé : base = durée prévue, puis heures excédentaires
   majorées. Exemple vérifié : prévu 13h, sortie 15h ⇒ 3h × 2.00 + 2h × 3.00 = **12.00** (et non 16.00).
3. **Refus `@PreAuthorize` renvoyé en 500** — `GlobalExceptionHandler` capturait `AccessDeniedException`
   via son gestionnaire générique. Ajout d'un gestionnaire 403 JSON (API) et d'une page HTML 403 (Web).
4. **Stub silencieux** — `VehiculeServiceImpl.getVehiculesByImmatriculation()` retournait une liste vide :
   désormais implémenté (recherche partielle insensible à la casse).

---

## 🎯 Ordre de Priorité Recommandé

1. **Étape 1 — Déblocage & Conformité Parking** : Réparer la compilation de `parking`, restructurer les DTOs (`request`/`response`), ajouter `AbonnementRepository` et uniformiser les entités/Lombok.
2. **Étape 2 — Couche Métier Parking** : Implémenter les Services, l'algorithme d'attribution de places, le calcul tarifaire avec gestion des dépassements et les entrées/sorties transactionnelles.
3. **Étape 3 — API REST & Exceptions Parking** : Implémenter les `@RestController` et le `@RestControllerAdvice`.
4. **Étape 4 — Interface Thymeleaf Parking** : Réaliser le dashboard, le plan interactif visuel des places (vert/rouge/bleu) et les formulaires.
5. **Étape 5 — Modélisation & Base Taxis** : Créer les entités, repositories, DTOs `request`/`response` et mappers du projet `taxis`.
6. **Étape 6 — Moteur Métier Taxis** : Implémenter l'algorithme d'attribution automatique avec score, la détection des conflits et le calcul des revenus/statistiques.
7. **Étape 7 — API REST & Thymeleaf Taxis** : Exposer les endpoints REST et créer les écrans Web Thymeleaf.
8. **Étape 8 — Sécurité Transversale (Security)** : Mettre en place Spring Security avec les 4 rôles sur les deux applications.
9. **Étape 9 — Finalisation, Pagination, Données de Démo & Tests** : Initialisation automatique de données représentatives (`DataInitializer`) et tests unitaires/intégrations.

---

## 🛠️ TODO Détaillé par Projet

---

### PARTIE 1 : PROJET PARKING (`/parking`)

#### 1.1. Réparation & Conformité Architecturale
- [x] **Corriger `TarifMapper.java`** : renommer la classe en `TarifMapper`, retirer le code copié-collé d'`AbonnementMapper` et mapper les champs réels de `Tarif` <-> `TarifResponse` / `TarifRequest`.
- [x] **Ajouter `AbonnementRepository.java`** dans `com.tpe.parking.repository` (manquant actuellement).
- [x] **Restructurer le package `dto` selon la norme imposée** :
  - Créer `com.tpe.parking.dto.request` :
    - [x] `CreateReservationRequest` (avec validations `@NotNull`, `@FutureOrPresent`, etc.)
    - [x] `UpdateReservationRequest`
    - [x] `CreateAbonnementRequest`
    - [x] `CreatePlaceRequest` / `UpdatePlaceRequest`
    - [x] `EntreeVehiculeRequest`
    - [x] `SortieVehiculeRequest`
    - [x] `CreateTarifRequest` / `UpdateTarifRequest`
    - [x] `CreateUtilisateurRequest`
    - [x] `CreateVehiculeRequest`
  - Créer `com.tpe.parking.dto.response` :
    - [x] `ReservationResponse` (sans exposer d'entités JPA directement)
    - [x] `AbonnementResponse` (remplacer les objets `Vehicule` et `Utilisateur` par des IDs ou DTOs imbriqués)
    - [x] `PlaceResponse`
    - [x] `EntreeSortieResponse`
    - [x] `TarifResponse`
    - [x] `UtilisateurResponse`
    - [x] `VehiculeResponse`
    - [x] `ParkingOccupancyResponse` (statistiques de taux d'occupation)
    - [x] `ParkingRevenueResponse` (chiffre d'affaires journalier/mensuel/global)
- [x] **Uniformiser les Entités et Lombok** :
  - [x] Corriger `Vehicule.java` : aligner le type de l'immatriculation (passer en `String` standard plutôt que `long`, ou clarifier la conversion).
  - [x] S'assurer que les getters/setters générés par Lombok ou manuels soient homogènes sur toutes les entités (`Place`, `Vehicule`, `Utilisateur`, `Abonnement`, `EntreeSortie`, `Tarif`).
  - [x] Ajouter les champs de sécurité dans `Utilisateur` ou créer une entité `CompteUtilisateur` (`email`, `motDePasse`, `role`).

#### 1.2. Gestion Centralisée des Exceptions
- [x] Créer les exceptions métier dans `com.tpe.parking.exception` :
  - [x] `ResourceNotFoundException`
  - [x] `BusinessException`
  - [x] `ConflictException` (ex: place déjà réservée sur ce créneau)
  - [x] `InvalidOperationException` (ex: entrée impossible, place déjà occupée)
  - [x] `PlaceUnavailableException`
- [x] Créer `ErrorResponse.java` dans `com.tpe.parking.dto.response` (timestamp, status, message, details, validationErrors).
- [x] Créer `GlobalExceptionHandler.java` avec `@RestControllerAdvice` pour intercepter toutes les exceptions et formater des réponses JSON avec codes HTTP adaptés (400, 404, 409, 500).

#### 1.3. Couche Métier (Services) & Logique Algorithmique
- [x] **Service de gestion des places** :
  - [x] `PlaceService` & `PlaceServiceImpl`
  - [x] Recherche des places disponibles par type et zone.
  - [x] **Moteur d'attribution automatique de place** :
    - Algorithme d'affectation optimisée : attribution place VIP si éligible, sinon règle de substitution ; attribution place PMR en priorité aux ayants droit ; réservation place standard.
- [x] **Service de tarification et calcul des dépassements** :
  - [x] `TarifService` & `TarifServiceImpl`
  - [x] Calcul du montant en fonction de : durée de stationnement, zone, type d'utilisateur/véhicule, statut d'abonnement (réduction appliquée).
  - [x] **Gestion stricte des dépassements** : comparaison de la date de départ prévue vs date de sortie réelle, facturation des heures additionnelles avec majoration paramétrée (`majorationApres`).
  - [x] Exemple validé : 15h prévue / 17h réelle => 2h supplémentaires facturées selon `majorationApres`.
- [x] **Service de gestion des réservations** :
  - [x] `ReservationService` & `ReservationServiceImpl`
  - [x] Vérification de non-chevauchement des réservations sur une même place (`@Transactional`).
  - [x] Annulation, validation, clôture de réservation.
- [x] **Service d'abonnements** :
  - [x] `AbonnementService` & `AbonnementServiceImpl`
  - [x] Création, renouvellement et calcul de validité (`estValide()`).
- [x] **Service Entrées / Sorties (Opérations critiques transactionnelles)** :
  - [x] `EntreeSortieService` & `EntreeSortieServiceImpl`
  - [x] **Entrée véhicule** (`@Transactional`) : vérification réservation / abonnement / place libre, passage de la place à `OCCUPEE`, horodatage de l'entrée.
  - [x] **Sortie véhicule** (`@Transactional`) : calcul de la durée, calcul du montant / dépassement éventuel, passage de la place à `LIBRE`, horodatage de sortie et archivage dans `EntreeSortie`.
- [x] **Service de statistiques** :
  - [x] `ParkingStatistiqueService` : taux d'occupation par zone/heure/jour, calcul du chiffre d'affaires, historique.

#### 1.4. Contrôleurs REST (`/api/...`)
- [x] `PlaceRestController` (`/api/places` - getAllPlaces, assignerPlace, libererPlace, transfererPlace)
- [x] `ReservationRestController` (`/api/reservations`, validation `@Valid`, `201 Created`, filtres par utilisateur/vehicule/place/statut)
- [x] `EntreeSortieRestController` (`/api/entrees-sorties` - enregistrerEntree, enregistrerSortie, getAllEntreesSorties)
- [x] `AbonnementRestController` (`/api/abonnements` - getAllAbonnements, souscrire, renouveler, resilier, check actif)
- [x] `TarifRestController` (`/api/tarifs` - getAllTarifs, getTarifApplicable, calculerMontant, calculerMontantAvecDepassement)
- [x] `ParkingStatsRestController` (`/api/stats` - taux d'occupation, revenus, totaux utilisateurs/vehicules/abonnements/places)
- [x] `UtilisateurRestController` - Amélioré avec `@RequiredArgsConstructor` et CRUD complet avec codes HTTP
- [x] `VehiculeRestController` - Déjà complet avec CRUD + filtrage par utilisateur

#### 1.5. Interface Web MVC Thymeleaf
- [x] Créer un layout Bootstrap propre (`fragments/layout.html` avec `head`, `nav`, `flash`, `footer`, `scripts`) + `static/css/app.css`.
- [x] **Dashboard principal** (`/dashboard`) :
  - [x] Résumé des indicateurs (places libres, occupées, réservées, CA du jour, véhicules présents, abonnements).
- [x] **Visualisation graphique du parking** :
  - [x] Grille visuelle interactive représentant les places avec code couleur :
    - 🟢 Vert : LIBRE
    - 🔴 Rouge : OCCUPÉE
    - 🔵 Bleu : RÉSERVÉE
    - ⚪ Gris : HORS SERVICE / PMR / VIP
  - [x] Groupement par zone + légende + compteurs (libres / occupées / réservées).
- [x] **Écrans de gestion** :
  - [x] `/places` : plan filtrable par zone / type / statut, tri zone+numéro.
  - [x] `/reservations` : formulaire de réservation (place souhaitée ou attribution automatique), tableau paginé, filtre par statut, annulation (OPERATOR+).
  - [x] `/abonnements` : souscription, liste paginée, statuts de validité calculés par le domaine, renouvellement (MANAGER+) et résiliation (ADMIN).
  - [x] `/borne` : borne d'entrée/sortie (plaque, place/zone optionnelles), ticket du dernier passage, véhicules présents, historique paginé filtrable.
  - [x] `/statistiques` : occupation globale et par zone, histogramme du CA journalier (période réglable), volumes gérés.
  - [x] Page d'erreur HTML (`error.html`) et page de connexion personnalisée (`login.html`).
  - [x] Protection CSRF activée sur les formulaires Thymeleaf.

---

### PARTIE 2 : PROJET TAXIS (`/taxis`)

#### 2.1. Modélisation JPA & Repositories
- [x] **Créer les entités dans `com.tpe.taxis.entity`** :
  - [x] `Role` : Enum (`ADMIN`, `MANAGER`, `OPERATOR`, `USER`).
  - [x] `Chauffeur` : `id`, `nom`, `prenom`, `telephone`, `numeroPermis`, `dateExpirationPermis`, `statut` (Enum: `DISPONIBLE`, `EN_COURSE`, `EN_PAUSE`, `INDISPONIBLE`, `SUSPENDU`), `dateEmbauche`.
  - [x] `Vehicule` : `id`, `immatriculation`, `marque`, `modele`, `annee`, `capacite`, `kilometrage`, `statut` (Enum: `DISPONIBLE`, `EN_COURSE`, `MAINTENANCE`, `HORS_SERVICE`), `chauffeurActuel`.
  - [x] `Client` : `id`, `nom`, `prenom`, `telephone`, `email`.
  - [x] `Course` : `id`, `client`, `pointDepart`, `destination`, `distanceKm`, `dateHeure`, `tarif`, `commission`, `montantChauffeur`, `chauffeur`, `vehicule`, `statut` (Enum: `EN_ATTENTE`, `ASSIGNEE`, `EN_COURS`, `TERMINEE`, `ANNULEE`).
  - [x] `ParametreTarif` : `id`, `tarifBase`, `prixKm`, `commissionPourcentage`.
  - [x] `CompteUtilisateur` : `id`, `username`, `motDePasse`, `role`, `chauffeur` (pour Spring Security).
- [x] **Créer les repositories dans `com.tpe.taxis.repository`** :
  - [x] `ChauffeurRepository` (méthodes : findByStatut, findEligiblesPourCourse, etc.)
  - [x] `VehiculeRepository` (méthodes : findByStatut, findByChauffeurActuelId, findDisponiblesPourCourse, etc.)
  - [x] `ClientRepository` (recherche paginée, findByTelephone/Email)
  - [x] `CourseRepository` (requêtes de statistiques de CA, filtres par statut, date et pagination)
  - [x] `ParametreTarifRepository`
  - [x] `CompteUtilisateurRepository` (findByUsername pour Spring Security)

#### 2.2. DTOs & Mappers
- [x] Créer `com.tpe.taxis.dto.request` :
  - [x] `CreateChauffeurRequest`, `UpdateChauffeurRequest`
  - [x] `CreateVehiculeRequest`, `UpdateVehiculeRequest`
  - [x] `CreateCourseRequest`, `AssignCourseRequest`
  - [x] `CreateClientRequest`, `UpdateClientRequest`
  - [x] `ParametreTarifRequest`
- [x] Créer `com.tpe.taxis.dto.response` :
  - [x] `ChauffeurResponse`
  - [x] `VehiculeResponse`
  - [x] `CourseResponse`
  - [x] `ClientResponse`
  - [x] `ParametreTarifResponse`
  - [x] `RevenueStatsResponse` (CA total, par chauffeur, par véhicule, commission)
  - [x] `ActivityStatsResponse` (taux annulation, chauffeur le plus actif, véhicule le plus utilisé, distance totale)
  - [x] `ErrorResponse`
- [x] Créer les mappers dans `com.tpe.taxis.mapper` (entités <-> DTOs) :
  - [x] `ChauffeurMapper`
  - [x] `VehiculeMapper`
  - [x] `ClientMapper`
  - [x] `CourseMapper`
  - [x] `ParametreTarifMapper`

#### 2.3. Gestion des Exceptions
- [x] Créer les exceptions spécifiques :
  - [x] `ResourceNotFoundException`
  - [x] `BusinessException`
  - [x] `PermisExpireException` (interdiction d'affecter une course à un permis expiré)
  - [x] `ChauffeurIndisponibleException` / `ChauffeurSuspenduException`
  - [x] `VehiculeEnMaintenanceException`
  - [x] `CourseConflictException` (course déjà attribuée ou chauffeur ayant déjà une course)
- [x] Créer `GlobalExceptionHandler` (`@RestControllerAdvice`) pour le projet Taxis.

#### 2.4. Couche Métier & Moteur d'Attribution Automatique
- [x] **Service Chauffeurs & Véhicules** :
  - [x] `ChauffeurService` & `ChauffeurServiceImpl` (CRUD, unicité du permis, recherche, filtres par statut, éligibles pour course, affectation/retrait de véhicule, suppression bloquée si en course).
  - [x] `VehiculeService` & `VehiculeServiceImpl` (CRUD, unicité de l'immatriculation, recherche, filtres par statut, changement de statut, affectation/retrait de chauffeur avec contrôles, suppression bloquée si en course).
- [x] **Service Clients** :
  - [x] `ClientService` & `ClientServiceImpl` (CRUD, unicité du téléphone, recherche paginée, suppression bloquée si historique de courses).
- [x] **Service Tarifs** :
  - [x] `ParametreTarifService` & `ParametreTarifServiceImpl` (CRUD des paramètres de tarification).
- [x] **Moteur d'Attribution (`CourseDispatcherService` & `CourseDispatcherServiceImpl`)** :
  - [x] Filtrage des chauffeurs éligibles :
    - Statut = `DISPONIBLE`
    - Date d'expiration du permis > date du jour (permis en cours de validité)
    - Véhicule associé disponible et pas en `MAINTENANCE`
  - [x] Affectation automatique d'un chauffeur éligible sans course active, avec véhicule associé ou fallback sur véhicule libre.
  - [x] Calcul d'un score de pertinence pour chaque chauffeur éligible :
    - Proximité : distance (estimée via `DistanceCalculator`) entre la dernière destination connue du chauffeur et le point de départ, normalisée sur [0,1] (1 = sur place, 0 au-delà de 15 km).
    - Équité : inverse du nombre de courses affectées sur une fenêtre glissante de 30 jours, normalisé par le maximum des candidats.
    - Coefficients configurables : `poidsProximite` (défaut 0.60) et `poidsEquite` (défaut 0.40) exposés dans `ParametreTarif` (entité, DTO request/response, mapper, validation).
    - Départage déterministe : ancienneté d'embauche en cas d'égalité de scores.
- [x] **Gestion des Conflits & Machine à États de la Course** (`CourseService` - `@Transactional`) :
  - [x] `createCourse(...)` : création en statut `EN_ATTENTE` avec tarification automatique (tarifBase + prixKm × distance).
  - [x] `assignerCourse(...)` : passage à `ASSIGNEE` avec contrôles (chauffeur éligible, pas de course active).
  - [x] `demarrerCourse(...)` : passage à `EN_COURS`, chauffeur & véhicule passent en `EN_COURSE`.
  - [x] `terminerCourse(...)` : passage à `TERMINEE`, actualisation du kilométrage véhicule, chauffeur & véhicule redeviennent `DISPONIBLE`.
  - [x] `annulerCourse(...)` : passage à `ANNULEE`, libération immédiate chauffeur/véhicule.
  - [x] Contrôles stricts : interdiction de double affectation (`existsByChauffeurIdAndStatutIn`), transitions de statut vérifiées.
- [x] **Module de Calcul des Revenus et Statistiques** :
  - [x] `TaxiStatistiqueService` & `TaxiStatistiqueServiceImpl`
  - [x] Chiffre d'affaires, commissions et part chauffeur sur une période (par chauffeur et par véhicule).
  - [x] Statistiques : chauffeur le plus actif, véhicule le plus utilisé, distance cumulée, taux d'annulation, filtrage par plage de dates (`dateDebut`, `dateFin`).

#### 2.5. Contrôleurs REST (`/api/...`)
- [x] `ChauffeurRestController` (`/api/chauffeurs` : CRUD complet, search, filtre par statut, éligibles, affectation/retrait véhicule).
- [x] `VehiculeRestController` (`/api/vehicules` : CRUD complet, search, filtre par statut, disponibles, affectation/retrait chauffeur).
- [x] `ClientRestController` (`/api/clients` : CRUD complet, recherche, filtre par téléphone).
- [x] `CourseRestController` (`/api/courses` : CRUD, filtres statut/client/chauffeur/véhicule/période, `/{id}/assigner`, `/{id}/demarrer`, `/{id}/terminer`, `/{id}/annuler`).
- [x] `CourseDispatcherRestController` (`/api/dispatch` : dispatch automatique à la création, dispatch d'une course existante, assignation manuelle).
- [x] `ParametreTarifRestController` (`/api/tarifs` : CRUD complet).
- [x] `TaxiStatsRestController` (`/api/stats/revenue`, `/api/stats/activity` avec filtrage par période).

#### 2.6. Interface Web MVC Thymeleaf
- [x] Layout Bootstrap cohérent avec navigation responsive (`layout.html` avec rôles dynamiques).
- [x] **Tableau de bord général** (`/dashboard`) : indicateurs en direct (courses du jour, actives, CA, taux d'annulation, flotte, courses à traiter).
- [x] **Gestion de la flotte & Disponibilité** (`/flotte`) : vue synoptique des véhicules et chauffeurs avec pastilles de statut (disponible, en course, maintenance, suspendu).
- [x] **Gestion des courses** (`/courses`) : listing avec filtres par statut et pagination, commande de course, dispatch automatique en 1 clic, cycle de vie (démarrer, terminer, annuler).
- [x] **Gestion des chauffeurs** (`/chauffeurs`) : fiches chauffeurs avec pagination, recherche, filtre statut, alerte visuelle si le permis expire, création/mise à jour et suspension.
- [x] **Gestion du parc de véhicules** (`/vehicules`) : recherche, filtre statut, attribution de chauffeur, mise en maintenance / sortie de maintenance.
- [x] **Gestion des clients** (`/clients`) : listing paginé, recherche multicritère, création et mise à jour des coordonnées clients.
- [x] **Rapports Financiers & Statistiques** (`/statistiques`) : histogramme journalier du CA sans dépendance JS, commissions société, part chauffeur, classements par chauffeur et véhicule, métriques d'activité sur période filtrable.
- [x] **Configuration des Tarifs & Dispatch** (`/tarifs`) : visualisation des formules, paramétrage du tarif de base, prix au km, commission et coefficients d'attribution (proximité et équité).
- [x] Page de connexion personnalisée (`/login`) et page d'erreur 403/404/500 (`error.html`).
- [x] Protection CSRF active sur tous les formulaires Thymeleaf.

---

### PARTIE 3 : EXIGENCES TRANSVERSALES (COMMUNES AUX DEUX PROJETS)

#### 3.1. Spring Security & Gestion des Rôles
- [x] Configurer `SecurityConfig` dans chaque projet :
  - [x] Support double accès : Form Login pour le Web MVC + HTTP Basic pour l'API REST (2 filter chains : `/api/**` stateless en Basic, reste en form login).
  - [x] Définition des 4 rôles via une matrice centralisée `SecurityPaths` (règles "METHODE /chemin" -> rôle minimum, hiérarchie USER < OPERATOR < MANAGER < ADMIN) :
    - `ADMIN` : accès complet (configuration des tarifs, suppressions).
    - `MANAGER` : gestion métier (utilisateurs/véhicules côté parking, CRUD flotte/clients/courses côté taxis).
    - `OPERATOR` : opérations quotidiennes (entrées/sorties, places, cycle de vie des courses, dispatch).
    - `USER` : consultation, réservation de place (parking) ou commande de course (taxis).
  - [x] Configuration de `PasswordEncoder` (BCrypt).
  - [x] `CustomUserDetailsService` dans chaque projet : parking = comptes `Utilisateur` (login par email), taxis = comptes `CompteUtilisateur` (login par username, lien optionnel chauffeur).
  - [x] Réponses 401/403 en JSON sur l'API (authenticationEntryPoint + accessDeniedHandler).
  - [x] Page de connexion Thymeleaf customisée (`/login`) sur les 2 projets.

#### 3.2. Recherche, Filtrage et Pagination
- [x] Parking — Intégrer `Pageable`, `Page<T>` et `Sort` sur toutes les listes volumineuses :
  - [x] REST : `page`, `size`, `sort` + filtres (`/api/places?zone&type&statut`, `/api/reservations?statut&utilisateurId&vehiculeId&placeId`, `/api/abonnements?statut&utilisateurId&vehiculeId`, `/api/entrees-sorties?enCours&immatriculation`, `/api/utilisateurs?recherche`, `/api/vehicules?recherche&utilisateurId`), requêtes JPQL `rechercher(...)` dans les repositories.
  - [x] Web Thymeleaf : pagination visuelle Précédent / numéros / Suivant en conservant les filtres courants (réservations, abonnements, historique borne, utilisateurs, véhicules).
- [x] Taxis — Même intégration complète :
  - [x] REST : `page`, `size`, `sort` + filtres (`/api/chauffeurs?recherche&statut`, `/api/vehicules?recherche&statut`, `/api/clients?telephone`, `/api/courses?statut&clientId&chauffeurId&dateDebut&dateFin`).
  - [x] Web Thymeleaf : pagination visuelle avec conservation des critères de recherche sur courses, chauffeurs, véhicules et clients.

#### 3.3. Jeux de Données de Démonstration (`DataInitializer`)
- [x] Créer un composant `@Component CommandLineRunner` dans chaque projet injectant automatiquement au démarrage :
  - [x] Comptes utilisateurs types pour chaque rôle (`admin`, `manager`, `operator`, `user`).
  - [x] Pour Parking : 24 places (2 zones, types PMR/VIP/Standard/Abonné), tarifs, abonnements actif/expiré, réservation à venir, entrée en cours + historique facturé.
  - [x] Pour Taxis : 5 chauffeurs (dont 1 permis expiré et 1 suspendu pour tester le blocage), 5 véhicules (dont 1 en maintenance), paramètres de tarif, 3 clients et courses à différents statuts.

#### 3.4. Tests Automatisés
- [x] Configurer les tests avec la base in-memory H2 (`src/test/resources/application.properties`).
- [x] Parking — 54 tests verts (`mvn test`) :
  - [x] Unitaires services : calculs tarifaires, tarif minimum, réduction abonné, **dépassement**, attribution automatique de place (réservation / abonné / substitution), entrée en double, parking complet, sortie sans entrée.
  - [x] Intégration MockMvc REST : 201 / 200 / 204 / 400 / 401 / 403 / 404 / 409, pagination et filtres.
  - [x] Web MockMvc : rendu de tous les écrans (`/dashboard`, `/places`, `/reservations`, `/abonnements`, `/borne`, `/statistiques`, `/tarifs`, `/utilisateurs`, `/vehicules`), CSRF obligatoire, refus 403 par rôle, messages flash.
  - [x] Script d'acceptation `test_all_endpoints.py` : **85/85** vérifications.
- [x] Taxis — 51 tests verts (`mvn test`) :
  - [x] Moteur d'attribution et scoring : proximité, équité, départage par ancienneté, exclusion permis expiré / suspendu / véhicule maintenance.
  - [x] Intégration MockMvc REST : codes HTTP stricts, cycles de vie de course, dispatch automatique, matrices 401/403.
  - [x] Web MockMvc : rendu de tous les écrans (`/dashboard`, `/courses`, `/flotte`, `/chauffeurs`, `/vehicules`, `/clients`, `/statistiques`, `/tarifs`), gestion client/chauffeur/tarifs, CSRF actif, refus 403.
  - [x] Script d'acceptation `test_all_taxis_endpoints.py`.

---

## 📅 Matrice d'Avancement Globale

```
[====================================================>] 100% (PROJETS PARKING ET TAXIS COMPLETS AVEC INTERFACES WEB THYMELEAF, REST ET SÉCURITÉ)
```

- [x] Audit complet des exigences du sujet `tpe.txt`
- [x] Analyse de l'existant sur `parking` et `taxis`
- [x] Identification des bugs bloquants de compilation
- [x] Étape 1 : Réparation compilation et DTOs `parking`
- [x] Étape 2 : Services & Métier `parking`
- [x] Étape 3 : API REST & Exceptions `parking` (REST Controllers COMPLETS)
- [x] Étape 4 : Interface Web Thymeleaf `parking` (9 écrans + login + error + CSRF)
- [x] Étape 5 : Arborescence complète des packages & fichiers squelettes `taxis`
- [x] Étape 6 : Services & Moteur d'attribution avec scoring (proximité + équité, coefficients configurables) `taxis`
- [x] Étape 7 : API REST & Interface Web `taxis` (REST & Web Thymeleaf 8 écrans + login + error + CSRF)
- [x] Étape 8 : Spring Security (4 rôles, double accès Basic/Form, matrice SecurityPaths) + DataInitializer de démo sur les 2 projets
- [x] Étape 9 : Tests unitaires, d'intégration REST & Web (105 tests automatisés au total : 54 parking, 51 taxis) + scripts d'acceptation python
