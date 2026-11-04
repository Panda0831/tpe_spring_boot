import base64
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

BASE_URL = "http://localhost:8084"

# Comptes de démonstration (voir auth.md) — l'API /api/** exige une authentification HTTP Basic.
ADMIN = "admin@parking.com:password"
MANAGER = "manager@parking.com:password"
OPERATOR = "operator@parking.com:password"
USER = "user@parking.com:password"

RESULTS = []


def request(method, path, body=None, params=None, auth=ADMIN):
    url = BASE_URL + path
    if params:
        url += "?" + urllib.parse.urlencode(params)

    headers = {"Content-Type": "application/json"} if body is not None else {}
    if auth:
        headers["Authorization"] = "Basic " + base64.b64encode(auth.encode()).decode()
    data = json.dumps(body).encode("utf-8") if body is not None else None

    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            status = resp.status
            content_type = resp.headers.get("Content-Type", "")
            raw = resp.read().decode("utf-8", errors="replace")
            try:
                parsed = json.loads(raw) if "json" in content_type else raw
            except Exception:
                parsed = raw
            return status, parsed, None
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", errors="replace")
        try:
            parsed = json.loads(raw)
        except Exception:
            parsed = raw
        return e.code, parsed, f"HTTPError {e.code}"
    except Exception as e:
        return 0, None, str(e)


def test(name, method, path, body=None, params=None, expected_status=None, auth=ADMIN):
    status, resp, err = request(method, path, body, params, auth)
    if expected_status is not None:
        if isinstance(expected_status, list):
            passed = status in expected_status
        else:
            passed = status == expected_status
    else:
        passed = 200 <= status < 300

    RESULTS.append(
        {
            "name": name,
            "method": method,
            "path": path,
            "status": status,
            "passed": passed,
            "error": err,
            "response": resp,
        }
    )
    status_icon = "✅" if passed else "❌"
    print(f"{status_icon} [{method}] {path} -> {status} ({name})")
    return resp


def content_of(page):
    """Extrait le tableau `content` d'une réponse paginée Spring Data."""
    if isinstance(page, dict) and "content" in page:
        return page["content"]
    return page if isinstance(page, list) else []


def main():
    print("=" * 70)
    print("TEST COMPLET DE TOUS LES ENDPOINTS DE L'API PARKING")
    print("=" * 70)

    # 1. Actuator (public)
    test("Actuator Health", "GET", "/actuator/health", expected_status=200, auth=None)

    # 2. Sécurité : sans authentification l'API répond 401
    test(
        "API sans authentification -> 401",
        "GET",
        "/api/places",
        expected_status=401,
        auth=None,
    )

    # 3. Parking Stats
    test(
        "Stats Utilisateurs Total",
        "GET",
        "/api/stats/utilisateurs/total",
        expected_status=200,
    )
    test(
        "Stats Vehicules Total",
        "GET",
        "/api/stats/vehicules/total",
        expected_status=200,
    )
    test(
        "Stats Abonnements Total",
        "GET",
        "/api/stats/abonnements/total",
        expected_status=200,
    )
    test("Stats Places Total", "GET", "/api/stats/places/total", expected_status=200)
    test(
        "Stats Entrees-Sorties Total",
        "GET",
        "/api/stats/entrees-sorties/total",
        expected_status=200,
    )
    test("Stats Tarifs Total", "GET", "/api/stats/tarifs/total", expected_status=200)
    test(
        "Stats Vehicules Presents",
        "GET",
        "/api/stats/vehicules-presents",
        expected_status=200,
    )
    test(
        "Stats Occupation Globale", "GET", "/api/stats/occupation", expected_status=200
    )
    test(
        "Stats Occupation par Zone",
        "GET",
        "/api/stats/occupation/zones",
        expected_status=200,
    )
    test("Stats Revenu du Jour", "GET", "/api/stats/revenus/jour", expected_status=200)
    test(
        "Stats Revenus 7 jours",
        "GET",
        "/api/stats/revenus",
        params={"jours": 7},
        expected_status=200,
    )

    # 4. Utilisateur CRUD
    ts = int(time.time() * 1000) % 1000000
    created_user = test(
        "Créer un Utilisateur",
        "POST",
        "/api/utilisateurs",
        body={
            "nom": "Martin",
            "prenom": "Claire",
            "telephone": "0601020304",
            "email": f"claire.martin.{ts}@test.fr",
            "motDePasse": "password123",
            "role": "USER",
        },
        expected_status=201,
    )

    user_id = created_user.get("id") if isinstance(created_user, dict) else 1

    test(
        "Lister tous les Utilisateurs (paginé)",
        "GET",
        "/api/utilisateurs",
        params={"page": 0, "size": 10, "sort": "nom,asc"},
        expected_status=200,
    )
    test(
        "Rechercher un Utilisateur",
        "GET",
        "/api/utilisateurs",
        params={"recherche": "martin", "page": 0, "size": 10},
        expected_status=200,
    )
    test(
        "Consulter Utilisateur par ID",
        "GET",
        f"/api/utilisateurs/{user_id}",
        expected_status=200,
    )
    test(
        "Modifier un Utilisateur",
        "PUT",
        f"/api/utilisateurs/{user_id}",
        body={
            "id": user_id,
            "nom": "Martin-Modifie",
            "prenom": "Claire",
            "telephone": "0601020304",
            "email": f"claire.martin.{ts}@test.fr",
            "role": "USER",
            "immatriculations": [],
        },
        expected_status=200,
    )

    # Sécurité : un USER ne peut pas créer d'utilisateur ni supprimer un tarif
    test(
        "Créer un Utilisateur en tant que USER -> 403",
        "POST",
        "/api/utilisateurs",
        body={
            "nom": "Interdit",
            "prenom": "Test",
            "telephone": "0600000000",
            "email": f"interdit.{ts}@test.fr",
            "role": "USER",
        },
        expected_status=403,
        auth=USER,
    )

    del_user = test(
        "Créer Utilisateur temporaire pour suppression",
        "POST",
        "/api/utilisateurs",
        body={
            "nom": "TempUser",
            "prenom": "Test",
            "telephone": "0699999999",
            "email": f"temp.user.{ts}@test.fr",
            "role": "USER",
        },
        expected_status=201,
    )
    del_user_id = del_user.get("id") if isinstance(del_user, dict) else None
    if del_user_id:
        test(
            "Supprimer un Utilisateur",
            "DELETE",
            f"/api/utilisateurs/{del_user_id}",
            expected_status=204,
        )

    # 5. Véhicule CRUD
    imm = f"AA-{ts % 1000:03d}-ZZ"
    created_vehicule = test(
        "Créer un Véhicule",
        "POST",
        "/api/vehicules",
        body={
            "immatriculation": imm,
            "marque": "Peugeot",
            "modele": "208",
            "utilisateurId": user_id,
        },
        expected_status=201,
    )
    vehicule_id = (
        created_vehicule.get("id") if isinstance(created_vehicule, dict) else 1
    )

    test(
        "Lister tous les Véhicules (paginé)",
        "GET",
        "/api/vehicules",
        params={"page": 0, "size": 10},
        expected_status=200,
    )
    test(
        "Rechercher un Véhicule",
        "GET",
        "/api/vehicules",
        params={"recherche": "peugeot", "page": 0, "size": 10},
        expected_status=200,
    )
    test(
        "Consulter Véhicule par ID",
        "GET",
        f"/api/vehicules/{vehicule_id}",
        expected_status=200,
    )
    test(
        "Lister Véhicules par Utilisateur",
        "GET",
        f"/api/vehicules/utilisateur/{user_id}",
        expected_status=200,
    )
    test(
        "Modifier un Véhicule",
        "PUT",
        f"/api/vehicules/{vehicule_id}",
        body={
            "immatriculation": imm,
            "marque": "Peugeot",
            "modele": "208 GT",
            "utilisateurId": user_id,
        },
        expected_status=200,
    )

    imm_del = f"DD-{(ts + 1) % 1000:03d}-ZZ"
    del_veh = test(
        "Créer Véhicule temporaire pour suppression",
        "POST",
        "/api/vehicules",
        body={
            "immatriculation": imm_del,
            "marque": "Renault",
            "modele": "Clio",
            "utilisateurId": user_id,
        },
        expected_status=201,
    )
    del_veh_id = del_veh.get("id") if isinstance(del_veh, dict) else None
    if del_veh_id:
        test(
            "Supprimer un Véhicule",
            "DELETE",
            f"/api/vehicules/{del_veh_id}",
            expected_status=204,
        )

    # 6. Places (pagination, tri, filtres)
    places = test(
        "Lister toutes les Places (paginé)",
        "GET",
        "/api/places",
        params={"page": 0, "size": 5, "sort": "numero,asc"},
        expected_status=200,
    )
    test(
        "Filtrer les Places par statut",
        "GET",
        "/api/places",
        params={"statut": "LIBRE", "page": 0, "size": 10},
        expected_status=200,
    )
    test(
        "Filtrer les Places par zone et type",
        "GET",
        "/api/places",
        params={"zone": "RDC", "type": "PMR", "page": 0, "size": 10},
        expected_status=200,
    )

    places_content = content_of(places)
    place_id = places_content[0]["id"] if places_content else 1

    test(
        "Assigner Place Parking à un utilisateur/véhicule",
        "POST",
        "/api/places/assigner",
        params={"utilisateurId": user_id, "vehiculeId": vehicule_id},
        expected_status=201,
    )
    test(
        "Assigner Place en tant que USER -> 403",
        "POST",
        "/api/places/assigner",
        params={"utilisateurId": user_id, "vehiculeId": vehicule_id},
        expected_status=403,
        auth=USER,
    )

    user2 = test(
        "Créer un second Utilisateur pour transfert",
        "POST",
        "/api/utilisateurs",
        body={
            "nom": "Durand",
            "prenom": "Paul",
            "telephone": "0605060708",
            "email": f"paul.durand.{ts}@test.fr",
            "role": "USER",
        },
        expected_status=201,
    )
    user2_id = user2.get("id") if isinstance(user2, dict) else user_id

    test(
        "Transférer Place Parking",
        "POST",
        "/api/places/transferer",
        params={
            "utilisateurSourceId": user_id,
            "utilisateurDestinationId": user2_id,
            "vehiculeId": vehicule_id,
        },
        expected_status=200,
    )
    test(
        "Libérer Place Parking",
        "POST",
        "/api/places/liberer",
        params={"utilisateurId": user2_id, "vehiculeId": vehicule_id},
        expected_status=200,
    )
    test(
        "Réassigner Place Parking",
        "POST",
        "/api/places/assigner",
        params={"utilisateurId": user_id, "vehiculeId": vehicule_id},
        expected_status=201,
    )

    # 7. Tarifs (CRUD réservé à ADMIN)
    test("Lister tous les Tarifs (DTO)", "GET", "/api/tarifs", expected_status=200)
    test(
        "Obtenir Tarif Applicable pour une zone",
        "GET",
        "/api/tarifs/applicable",
        params={"zone": "RDC"},
        expected_status=200,
    )
    test("Total des Tarifs", "GET", "/api/tarifs/total", expected_status=200)
    test(
        "Calculer Montant Tarif",
        "POST",
        "/api/tarifs/calculer-montant",
        params={
            "dateEntree": "2026-09-15T10:00:00",
            "dateSortie": "2026-09-15T14:00:00",
            "zone": "RDC",
            "abonne": "false",
            "role": "USER",
            "typeVehicule": "VOITURE",
        },
        expected_status=200,
    )
    test(
        "Calculer Montant avec Dépassement",
        "POST",
        "/api/tarifs/calculer-montant-depassement",
        params={
            "dateEntree": "2026-09-15T10:00:00",
            "dateSortie": "2026-09-15T16:00:00",
            "dateDepartPrevue": "2026-09-15T14:00:00",
            "zone": "RDC",
            "abonne": "false",
            "role": "USER",
            "typeVehicule": "VOITURE",
        },
        expected_status=200,
    )

    created_tarif = test(
        "Créer un Tarif",
        "POST",
        "/api/tarifs",
        body={
            "zone": f"ZONE-TEST-{ts % 100}",
            "montantParHeure": 3.50,
            "tarifMinimum": 2.00,
            "forfaitJournalier": 20.00,
            "reductionAbonne": 0.10,
            "majorationApres": 0.50,
        },
        expected_status=201,
    )
    tarif_id = created_tarif.get("id") if isinstance(created_tarif, dict) else None
    if tarif_id:
        test(
            "Consulter un Tarif par ID",
            "GET",
            f"/api/tarifs/{tarif_id}",
            expected_status=200,
        )
        test(
            "Modifier un Tarif",
            "PUT",
            f"/api/tarifs/{tarif_id}",
            body={"montantParHeure": 4.00},
            expected_status=200,
        )
        test(
            "Créer un Tarif en tant que OPERATOR -> 403",
            "POST",
            "/api/tarifs",
            body={"zone": f"ZONE-INTERDITE-{ts % 100}", "montantParHeure": 1.00},
            expected_status=403,
            auth=OPERATOR,
        )
        test(
            "Supprimer un Tarif",
            "DELETE",
            f"/api/tarifs/{tarif_id}",
            expected_status=204,
        )
    test(
        "Consulter un Tarif inexistant -> 404",
        "GET",
        "/api/tarifs/999999",
        expected_status=404,
    )

    # 8. Abonnements
    imm_abo = f"AB-{(ts + 2) % 1000:03d}-ZZ"
    veh_abo = test(
        "Créer Véhicule pour Abonnement",
        "POST",
        "/api/vehicules",
        body={
            "immatriculation": imm_abo,
            "marque": "Toyota",
            "modele": "Yaris",
            "utilisateurId": user_id,
        },
        expected_status=201,
    )
    veh_abo_id = veh_abo.get("id") if isinstance(veh_abo, dict) else vehicule_id

    created_abo = test(
        "Souscrire Abonnement",
        "POST",
        "/api/abonnements",
        body={
            "utilisateurId": user_id,
            "vehiculeId": veh_abo_id,
            "type": "MENSUEL",
            "dateDebut": "2026-09-15",
            "dateFin": "2026-10-15",
        },
        expected_status=201,
    )
    abo_id = created_abo.get("id") if isinstance(created_abo, dict) else 1

    test(
        "Souscrire Abonnement en tant que USER -> 403",
        "POST",
        "/api/abonnements",
        body={
            "utilisateurId": user_id,
            "vehiculeId": veh_abo_id,
            "type": "MENSUEL",
            "dateDebut": "2027-03-01",
            "dateFin": "2027-04-01",
        },
        expected_status=403,
        auth=USER,
    )
    test(
        "Lister tous les Abonnements (paginé)",
        "GET",
        "/api/abonnements",
        params={"page": 0, "size": 10, "sort": "dateFin,asc"},
        expected_status=200,
    )
    test(
        "Filtrer les Abonnements par statut",
        "GET",
        "/api/abonnements",
        params={"statut": "ACTIF", "page": 0, "size": 10},
        expected_status=200,
    )
    test(
        "Consulter Abonnement par ID",
        "GET",
        f"/api/abonnements/{abo_id}",
        expected_status=200,
    )
    test(
        "Lister Abonnements par Utilisateur",
        "GET",
        f"/api/abonnements/utilisateur/{user_id}",
        expected_status=200,
    )
    test(
        "Lister Abonnements par Véhicule",
        "GET",
        f"/api/abonnements/vehicule/{veh_abo_id}",
        expected_status=200,
    )
    test(
        "Lister Abonnements par Statut",
        "GET",
        "/api/abonnements/statut/ACTIF",
        expected_status=200,
    )
    test(
        "Vérifier Abonnement Actif par Immatriculation",
        "GET",
        f"/api/abonnements/immatriculation/{imm_abo}/actif",
        expected_status=200,
    )
    test(
        "Vérifier Abonnement Actif par Véhicule ID",
        "GET",
        f"/api/abonnements/vehicule/{veh_abo_id}/actif",
        expected_status=200,
    )
    test(
        "Vérifier Abonnement Actif par Utilisateur ID",
        "GET",
        f"/api/abonnements/utilisateur/{user_id}/actif",
        expected_status=200,
    )
    test(
        "Renouveler Abonnement",
        "PUT",
        f"/api/abonnements/{abo_id}/renouveler",
        params={"nouvelleDateFin": "2026-11-15"},
        expected_status=200,
    )
    test(
        "Résilier Abonnement",
        "DELETE",
        f"/api/abonnements/{abo_id}",
        expected_status=204,
    )

    # 9. Réservations
    created_resa = test(
        "Créer Réservation",
        "POST",
        "/api/reservations",
        body={
            "utilisateurId": user_id,
            "vehiculeId": vehicule_id,
            "placeId": place_id,
            "dateArriveePrevue": "2026-11-20T10:00:00",
            "dateDepartPrevue": "2026-11-20T18:00:00",
        },
        expected_status=201,
    )
    resa_id = created_resa.get("id") if isinstance(created_resa, dict) else 1

    test(
        "Créer Réservation invalide -> 400",
        "POST",
        "/api/reservations",
        body={"utilisateurId": user_id, "vehiculeId": vehicule_id},
        expected_status=400,
    )
    test(
        "Créer Réservation en conflit -> 409",
        "POST",
        "/api/reservations",
        body={
            "utilisateurId": user2_id,
            "vehiculeId": vehicule_id,
            "placeId": place_id,
            "dateArriveePrevue": "2026-11-20T11:00:00",
            "dateDepartPrevue": "2026-11-20T19:00:00",
        },
        expected_status=409,
    )
    test(
        "Lister toutes les Réservations (paginé)",
        "GET",
        "/api/reservations",
        params={"page": 0, "size": 10, "sort": "dateArriveePrevue,desc"},
        expected_status=200,
    )
    test(
        "Filtrer les Réservations par statut",
        "GET",
        "/api/reservations",
        params={"statut": "EN_ATTENTE", "page": 0, "size": 10},
        expected_status=200,
    )
    test(
        "Consulter Réservation par ID",
        "GET",
        f"/api/reservations/{resa_id}",
        expected_status=200,
    )
    test(
        "Lister Réservations par Utilisateur",
        "GET",
        f"/api/reservations/utilisateur/{user_id}",
        expected_status=200,
    )
    test(
        "Lister Réservations par Véhicule",
        "GET",
        f"/api/reservations/vehicule/{vehicule_id}",
        expected_status=200,
    )
    test(
        "Lister Réservations par Place",
        "GET",
        f"/api/reservations/place/{place_id}",
        expected_status=200,
    )
    test(
        "Lister Réservations par Statut (chemin)",
        "GET",
        "/api/reservations/statut/EN_ATTENTE",
        expected_status=200,
    )
    test(
        "Modifier une Réservation",
        "PUT",
        f"/api/reservations/{resa_id}",
        body={
            "placeId": place_id,
            "dateArriveePrevue": "2026-11-20T11:00:00",
            "dateDepartPrevue": "2026-11-20T19:00:00",
            "statut": "CONFIRMEE",
        },
        expected_status=200,
    )
    test(
        "Vérifier Abonnement Actif via Reservation controller",
        "GET",
        f"/api/reservations/utilisateur/{user_id}/abonnement-actif",
        expected_status=200,
    )
    test(
        "Annuler une Réservation",
        "DELETE",
        f"/api/reservations/{resa_id}",
        expected_status=204,
    )

    imm_resa2 = f"RS-{(ts + 3) % 1000:03d}-ZZ"
    veh_resa2 = test(
        "Créer véhicule pour résa à supprimer",
        "POST",
        "/api/vehicules",
        body={
            "immatriculation": imm_resa2,
            "marque": "Citroen",
            "modele": "C3",
            "utilisateurId": user_id,
        },
        expected_status=201,
    )
    veh_resa2_id = veh_resa2.get("id") if isinstance(veh_resa2, dict) else vehicule_id
    del_resa = test(
        "Créer Réservation temporaire pour suppression",
        "POST",
        "/api/reservations",
        body={
            "utilisateurId": user_id,
            "vehiculeId": veh_resa2_id,
            "dateArriveePrevue": "2026-11-25T10:00:00",
            "dateDepartPrevue": "2026-11-25T12:00:00",
        },
        expected_status=201,
    )
    del_resa_id = del_resa.get("id") if isinstance(del_resa, dict) else None
    if del_resa_id:
        test(
            "Supprimer définitivement Réservation",
            "DELETE",
            f"/api/reservations/{del_resa_id}/supprimer",
            expected_status=204,
        )

    # 10. Entrées / Sorties (corps JSON avec immatriculation)
    test(
        "Lister Entrées/Sorties (paginé)",
        "GET",
        "/api/entrees-sorties",
        params={"page": 0, "size": 10, "sort": "dateEntree,desc"},
        expected_status=200,
    )
    test(
        "Filtrer Entrées/Sorties en cours",
        "GET",
        "/api/entrees-sorties",
        params={"enCours": "true", "page": 0, "size": 10},
        expected_status=200,
    )
    test(
        "Enregistrer Entrée Véhicule",
        "POST",
        "/api/entrees-sorties/entree",
        body={"immatriculation": imm},
        expected_status=201,
    )
    test(
        "Enregistrer Entrée en double -> 400",
        "POST",
        "/api/entrees-sorties/entree",
        body={"immatriculation": imm},
        expected_status=400,
    )
    test(
        "Enregistrer Sortie Véhicule",
        "POST",
        "/api/entrees-sorties/sortie",
        body={"immatriculation": imm},
        expected_status=200,
    )
    test(
        "Enregistrer Entrée en tant que USER -> 403",
        "POST",
        "/api/entrees-sorties/entree",
        body={"immatriculation": imm},
        expected_status=403,
        auth=USER,
    )

    print("\n" + "=" * 70)
    print("RÉCAPITULATIF DE L'EXÉCUTION DES TESTS")
    print("=" * 70)
    total = len(RESULTS)
    passed = sum(1 for r in RESULTS if r["passed"])
    failed = total - passed
    print(f"Total endpoints testés : {total}")
    print(f"Succès                : {passed}")
    print(f"Échecs                : {failed}")
    print("=" * 70)

    if failed > 0:
        print("\nDÉTAILS DES ÉCHECS :")
        for r in RESULTS:
            if not r["passed"]:
                print(
                    f" [{r['method']}] {r['path']} -> Reçu: {r['status']}, Erreur: {r['error']}, Resp: {r['response']}"
                )
        sys.exit(1)
    else:
        print("\n TOUS LES ENDPOINTS ONT RÉPONDU AVEC SUCCÈS (100% DE RÉUSSITE) !")


if __name__ == "__main__":
    main()
