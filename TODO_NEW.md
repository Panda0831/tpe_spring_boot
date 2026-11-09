

1

# TODO - Migration Frontend Angular

## Projet Parking

- [X] Initialiser l'application Angular (`client/`)
- [X] Configurer le proxy vers le backend Spring Boot (port 8084)
- [X] Créer les composants (Dashboard, Places, Réservations, Abonnements, Borne, Statistiques, Login)
- [X] Créer les services pour les appels API
- [X] Mettre en place l'authentification et les guards
- [ ] Connecter et tester les appels API avec le backend parking
- [ ] Adapter les templates pour un affichage complet
- [ ] Tester l'intégration complète

## Projet Taxis

- [X] Initialiser l'application Angular (`taxis-client/`)
- [X] Configurer le proxy vers le backend Spring Boot (port 8081)
- [X] Créer les composants (Dashboard, Courses, Chauffeurs, Véhicules, Clients, Flotte, Tarifs, Statistiques, Login)
- [X] Créer le service Taxis pour les appels API
- [X] Mettre en place l'authentification et les guards
- [X] Connecter les appels API au contrat backend (pagination Spring Data, CRUD, dispatch, cycle de vie des courses, stats, tarifs) — `taxis-client/src/app/services/taxis.service.ts`
- [X] Adapter les templates pour un affichage complet (formulaires, tableaux, actions, stats) + Bootstrap CDN
- [X] Tester l'intégration API avec le backend taxis lancé (port 8081) — `python test_all_taxis_endpoints.py` (38/38 sur H2 ; PostgreSQL local indisponible sur le port 5434)

## Général

- [ ] Lancer et vérifier les deux backends Spring Boot
- [ ] Lancer et vérifier les deux frontends Angular
- [ ] Tester l'authentification sur les deux applications
- [ ] Vérifier la communication API entre frontend et backend
