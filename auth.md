# 🔐 Identifiants & Authentification — Projets TPE

> Les comptes ci-dessous sont créés **automatiquement au premier démarrage** par les
> `DataInitializer` (voir `*/src/main/java/com/tpe/*/config/DataInitializer.java`).
> Le mot de passe est stocké **haché en BCrypt** en base.
>
> ⚠️ Comptes de démonstration pour un projet scolaire : ne jamais faire cela en production.

---

## 🅿️ Projet Parking — `http://localhost:8084`

Identifiant de connexion = **email** du compte.

| Email | Mot de passe | Rôle | Droits |
|---|---|---|---|
| `admin@parking.com` | `password` | ADMIN | Tout : configuration des tarifs, suppressions |
| `manager@parking.com` | `password` | MANAGER | Gestion métier : utilisateurs, véhicules, renouvellement abonnements |
| `operator@parking.com` | `password` | OPERATOR | Opérations quotidiennes : entrées/sorties, places (assigner/libérer/transférer), annulation réservation |
| `user@parking.com` | `password` | USER | Consultation + réservation de place |
| `vip@parking.com` | `password` | USER | Client VIP de démonstration (abonnement expiré) |

Hiérarchie : un rôle supérieur hérite des droits des rôles inférieurs
(`ADMIN` ⊃ `MANAGER` ⊃ `OPERATOR` ⊃ `USER`).

---

## 🚕 Projet Taxis — `http://localhost:8081`

Identifiant de connexion = **username** du compte.

| Username | Mot de passe | Rôle | Droits |
|---|---|---|---|
| `admin` | `password` | ADMIN | Tout : configuration des tarifs, suppressions |
| `manager` | `password` | MANAGER | Gestion métier : chauffeurs, véhicules, clients, annulation de course |
| `operator` | `password` | OPERATOR | Opérations quotidiennes : assigner/démarrer/terminer une course, dispatch |
| `user` | `password` | USER | Consultation + commande de course |
| `chauffeur1` | `password` | USER | Compte lié au chauffeur Jonathan Kabasele (PERM-001) |

Jeux de données de blocage inclus pour tester le dispatch :
- `PERM-004-EXPIRE` (Alain Tshibangu) : permis expiré → ne reçoit jamais de course
- Chantal Nsimba : statut SUSPENDU → ne reçoit jamais de course
- Véhicule `CD-004-GH` : en MAINTENANCE → exclu du dispatch

---

## 🌐 Deux modes d'accès

### 1. Interface Web (Thymeleaf) — Form login avec session
```
Parking : http://localhost:8084/login
Taxis   : http://localhost:8081/login
```
*(la page de connexion customisée sera créée avec les écrans Thymeleaf — Étapes 4 et 7)*

### 2. API REST — HTTP Basic (sans état)
Toutes les requêtes `/api/**` exigent une authentification Basic ; les réponses
d'erreur sont en JSON : `401` si non authentifié, `403` si rôle insuffisant.

**Exemples :**

```bash
# Parking : lecture des statistiques (tout rôle authentifié)
curl -u admin@parking.com:password http://localhost:8084/api/stats/places/total

# Parking : réservation (réservé à USER et plus)
curl -u user@parking.com:password -H "Content-Type: application/json" \
     -d '{"utilisateurId":4,"vehiculeId":1,"dateArriveePrevue":"2026-09-23T10:00:00","dateDepartPrevue":"2026-09-23T13:00:00"}' \
     http://localhost:8084/api/reservations

# Taxis : statistiques de chiffre d'affaires
curl -u manager:password http://localhost:8081/api/stats/revenue?dateDebut=2026-09-01T00:00:00&dateFin=2026-09-30T23:59:59

# Taxis : configuration des tarifs (ADMIN uniquement — 403 pour les autres rôles)
curl -u admin:password -H "Content-Type: application/json" \
     -d '{"tarifBase":2.50,"prixKm":1.80,"commissionPourcentage":20,"poidsProximite":0.6,"poidsEquite":0.4}' \
     http://localhost:8081/api/tarifs

# Vérifier le refus 403 : un USER ne peut pas configurer les tarifs
curl -i -u user:password -X POST http://localhost:8081/api/tarifs
```

### Pagination, tri et filtres (API REST)

Les listes volumineuses renvoient une page Spring Data (`content`, `totalElements`,
`totalPages`, `number`, `size`) et acceptent `page`, `size`, `sort` :

```bash
# Places : page 0, 10 par page, tri par numéro décroissant, filtrées par statut et zone
curl -u admin@parking.com:password -G http://localhost:8084/api/places \
  -d page=0 -d size=10 -d sort=numero,desc -d statut=LIBRE -d zone=RDC

# Réservations : filtre statut + période
curl -u manager@parking.com:password -G http://localhost:8084/api/reservations \
  -d statut=EN_ATTENTE -d utilisateurId=4 -d size=5

# Entrées/sorties : stationnements en cours uniquement
curl -u operator@parking.com:password -G http://localhost:8084/api/entrees-sorties -d enCours=true

# Utilisateurs / véhicules : recherche plein texte
curl -u manager@parking.com:password -G http://localhost:8084/api/utilisateurs -d recherche=kasongo
```

### Borne et statistiques

```bash
# Entrée d'un véhicule (le système attribue une place : réservation > abonné > zone > substitution)
curl -u operator@parking.com:password -H "Content-Type: application/json" \
     -d '{"immatriculation":"CD-AAA-002"}' http://localhost:8084/api/entrees-sorties/entree

# Sortie : durée réelle, réduction abonné et majoration des heures de dépassement
curl -u operator@parking.com:password -H "Content-Type: application/json" \
     -d '{"immatriculation":"CD-AAA-002"}' http://localhost:8084/api/entrees-sorties/sortie

# Occupation (globale et par zone) et chiffre d'affaires
curl -u user@parking.com:password http://localhost:8084/api/stats/occupation
curl -u user@parking.com:password http://localhost:8084/api/stats/occupation/zones
curl -u user@parking.com:password http://localhost:8084/api/stats/revenus?jours=7

# Configuration des tarifs : réservée à ADMIN (403 pour les autres rôles)
curl -u admin@parking.com:password -H "Content-Type: application/json" \
     -d '{"zone":"TOIT","montantParHeure":3.50,"tarifMinimum":2.00}' http://localhost:8084/api/tarifs
```

---

## 🖥️ Interface Web (Thymeleaf)

```
Parking : http://localhost:8084/login
```

| Écran | URL | Rôles |
|---|---|---|
| Tableau de bord (indicateurs, occupation par zone) | `/dashboard` | tous |
| Plan du parking (grille colorée, filtres zone/type/statut) | `/places` | tous |
| Réservations (créer, consulter, annuler) | `/reservations` | tous (annulation : OPERATOR+) |
| Abonnements (souscrire, renouveler, résilier) | `/abonnements` | tous (souscription : MANAGER+, résiliation : ADMIN) |
| Borne entrées/sorties (ticket, historique) | `/borne` | OPERATOR+ |
| Statistiques d'occupation et de revenus | `/statistiques` | tous |

Les formulaires sont protégés par un jeton CSRF (généré automatiquement par Thymeleaf) ;
un POST sans jeton reçoit une réponse `403`.

---

## 🛠️ Dépannage

**Les comptes n'existent pas / connexion refusée ?**
Le `DataInitializer` n'injecte les données que si les tables sont **vides**.
Si la base a été peuplée avant l'ajout de Spring Security (mots de passe non hachés),
purgez les données de démonstration puis relancez l'application :

```bash
# via psql (adapter host/port/db)
psql -h localhost -p 5434 -U fanamby -d parking_db -c \
  "TRUNCATE entrees_sorties, reservations, abonnements, vehicules, places, tarifs, utilisateurs RESTART IDENTITY CASCADE;"
```

**Récapitulatif des ports**

| Projet | Port | Base PostgreSQL |
|---|---|---|
| parking | 8084 | `parking_db` (localhost:5434) |
| taxis | 8081 | `taxis_db` (localhost:5434) |
