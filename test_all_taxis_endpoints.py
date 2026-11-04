import base64
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

BASE_URL = "http://localhost:8081"

# Comptes de démonstration (voir auth.md) — l'API /api/** exige une authentification HTTP Basic.
ADMIN = "admin:password"
MANAGER = "manager:password"
OPERATOR = "operator:password"
USER = "user:password"

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
    print("TEST COMPLET DE TOUS LES ENDPOINTS DE L'API TAXIS")
    print("=" * 70)

    # 1. Actuator (public)
    test("Actuator Health", "GET", "/actuator/health", expected_status=200, auth=None)

    # 2. Sécurité : sans authentification l'API répond 401
    test("API sans authentification -> 401", "GET", "/api/chauffeurs", expected_status=401, auth=None)

    # 3. Statistiques Taxis
    test("Stats Chiffre d'Affaires Global", "GET", "/api/stats/revenue", expected_status=200)
    test("Stats Activité Globale", "GET", "/api/stats/activity", expected_status=200)
    test("Stats Activité Période", "GET", "/api/stats/activity",
         params={"dateDebut": "2026-01-01T00:00:00", "dateFin": "2026-12-31T23:59:59"},
         expected_status=200)

    # 4. Paramètres de Tarifs
    tarifs = test("Lister les Paramètres Tarifaires", "GET", "/api/tarifs", expected_status=200)
    tarif_id = tarifs[0]["id"] if isinstance(tarifs, list) and len(tarifs) > 0 else 1
    test("Consulter Tarif par ID", "GET", f"/api/tarifs/{tarif_id}", expected_status=200)

    # Sécurité Tarif : un USER ne peut pas modifier le tarif (403)
    test("USER modifie Tarif -> 403", "PUT", f"/api/tarifs/{tarif_id}",
         body={"tarifBase": 3.0, "prixKm": 2.0, "commissionPourcentage": 20.0},
         expected_status=403, auth=USER)

    # 5. Clients CRUD
    ts = int(time.time() * 1000) % 1000000
    created_client = test("Créer un Client", "POST", "/api/clients",
                          body={"nom": "Mbuyi", "prenom": "Patricia",
                                "telephone": f"+24381{ts:07d}", "email": f"client.{ts}@taxis.cd"},
                          expected_status=201, auth=MANAGER)
    client_id = created_client.get("id") if isinstance(created_client, dict) else 1

    test("Lister Clients (paginé)", "GET", "/api/clients", params={"page": 0, "size": 10}, expected_status=200)
    test("Rechercher Client par téléphone", "GET", "/api/clients",
         params={"telephone": created_client.get("telephone", "+24381")}, expected_status=200)
    test("Consulter Client par ID", "GET", f"/api/clients/{client_id}", expected_status=200)
    test("Modifier Client", "PUT", f"/api/clients/{client_id}",
         body={"nom": "Mbuyi-Modifie", "prenom": "Patricia", "telephone": created_client.get("telephone")},
         expected_status=200, auth=MANAGER)

    # 6. Chauffeurs CRUD
    created_chauffeur = test("Créer un Chauffeur", "POST", "/api/chauffeurs",
                             body={"nom": "Ilunga", "prenom": "Serge",
                                   "telephone": f"+24399{ts:07d}", "numeroPermis": f"PERM-{ts}",
                                   "dateExpirationPermis": "2030-12-31", "statut": "DISPONIBLE"},
                             expected_status=201, auth=MANAGER)
    chauffeur_id = created_chauffeur.get("id") if isinstance(created_chauffeur, dict) else 1

    test("Lister Chauffeurs (paginé)", "GET", "/api/chauffeurs", params={"page": 0, "size": 10}, expected_status=200)
    test("Lister Chauffeurs Éligibles", "GET", "/api/chauffeurs/eligible", expected_status=200)
    test("Consulter Chauffeur par ID", "GET", f"/api/chauffeurs/{chauffeur_id}", expected_status=200)
    test("Changer statut Chauffeur", "PATCH", f"/api/chauffeurs/{chauffeur_id}/statut",
         params={"statut": "EN_PAUSE"}, expected_status=200, auth=MANAGER)

    # 7. Véhicules CRUD
    created_vehicule = test("Créer un Véhicule", "POST", "/api/vehicules",
                            body={"immatriculation": f"CD-{ts % 1000:03d}-TX",
                                  "marque": "Toyota", "modele": "Corolla", "annee": 2022,
                                  "capacite": 4, "kilometrage": 50000.0, "statut": "DISPONIBLE"},
                            expected_status=201, auth=MANAGER)
    vehicule_id = created_vehicule.get("id") if isinstance(created_vehicule, dict) else 1

    test("Lister Véhicules (paginé)", "GET", "/api/vehicules", params={"page": 0, "size": 10}, expected_status=200)
    test("Lister Véhicules Disponibles", "GET", "/api/vehicules/disponibles", expected_status=200)
    test("Consulter Véhicule par ID", "GET", f"/api/vehicules/{vehicule_id}", expected_status=200)

    # Affecter véhicule au chauffeur
    test("Affecter Véhicule au Chauffeur", "PATCH", f"/api/chauffeurs/{chauffeur_id}/vehicule/{vehicule_id}",
         expected_status=200, auth=MANAGER)
    test("Remettre Chauffeur en DISPONIBLE", "PATCH", f"/api/chauffeurs/{chauffeur_id}/statut",
         params={"statut": "DISPONIBLE"}, expected_status=200, auth=MANAGER)

    # 8. Courses & Machine à États
    created_course = test("Commander une Course (USER)", "POST", "/api/courses",
                          body={"clientId": client_id, "pointDepart": "Centre-Ville",
                                "destination": "Gare Centrale", "distanceKm": 5.5},
                          expected_status=201, auth=USER)
    course_id = created_course.get("id") if isinstance(created_course, dict) else 1

    test("Lister les Courses", "GET", "/api/courses", params={"page": 0, "size": 10}, expected_status=200)
    test("Consulter Course par ID", "GET", f"/api/courses/{course_id}", expected_status=200)

    # Dispatch automatique
    test("Dispatch automatique de la Course (OPERATOR)", "POST", f"/api/dispatch/{course_id}",
         expected_status=200, auth=OPERATOR)

    # Cycle de vie de la Course
    test("Démarrer la Course (OPERATOR)", "POST", f"/api/courses/{course_id}/demarrer",
         expected_status=200, auth=OPERATOR)
    test("Terminer la Course (OPERATOR)", "POST", f"/api/courses/{course_id}/terminer",
         expected_status=200, auth=OPERATOR)

    # 9. Créer et Assigner en 1 appel (Dispatcher)
    dispatch_direct = test("Créer et Assigner Course via Dispatch", "POST", "/api/dispatch/creer-et-assigner",
                           body={"clientId": client_id, "pointDepart": "Aéroport",
                                 "destination": "Hôtel Memling", "distanceKm": 18.0},
                           expected_status=200, auth=OPERATOR)
    course_direct_id = dispatch_direct.get("id") if isinstance(dispatch_direct, dict) else None

    if course_direct_id:
        test("Annuler Course (MANAGER)", "POST", f"/api/courses/{course_direct_id}/annuler",
             expected_status=200, auth=MANAGER)
        test("Supprimer Course (ADMIN)", "DELETE", f"/api/courses/{course_direct_id}",
             expected_status=204, auth=ADMIN)

    # 10. Nettoyage (ADMIN)
    test("Retirer Véhicule du Chauffeur", "DELETE", f"/api/chauffeurs/{chauffeur_id}/vehicule",
         expected_status=200, auth=MANAGER)
    test("Supprimer Chauffeur (ADMIN)", "DELETE", f"/api/chauffeurs/{chauffeur_id}",
         expected_status=204, auth=ADMIN)
    test("Supprimer Véhicule (ADMIN)", "DELETE", f"/api/vehicules/{vehicule_id}",
         expected_status=204, auth=ADMIN)

    # Bilan
    print("=" * 70)
    total = len(RESULTS)
    reussis = sum(1 for r in RESULTS if r["passed"])
    echoues = total - reussis
    print(f"BILAN DES TESTS API TAXIS : {reussis}/{total} réussis ({echoues} échec(s))")
    print("=" * 70)
    if echoues > 0:
        sys.exit(1)


if __name__ == "__main__":
    main()
