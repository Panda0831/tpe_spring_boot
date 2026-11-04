package com.tpe.taxis.config;

import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.ChauffeurStatut;
import com.tpe.taxis.entity.Client;
import com.tpe.taxis.entity.CompteUtilisateur;
import com.tpe.taxis.entity.Course;
import com.tpe.taxis.entity.CourseStatut;
import com.tpe.taxis.entity.ParametreTarif;
import com.tpe.taxis.entity.Role;
import com.tpe.taxis.entity.Vehicule;
import com.tpe.taxis.entity.VehiculeStatut;
import com.tpe.taxis.repository.ChauffeurRepository;
import com.tpe.taxis.repository.ClientRepository;
import com.tpe.taxis.repository.CompteUtilisateurRepository;
import com.tpe.taxis.repository.CourseRepository;
import com.tpe.taxis.repository.ParametreTarifRepository;
import com.tpe.taxis.repository.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Jeu de données de démonstration (exigence 3.3 du plan) :
 * <ul>
 *   <li>un compte par rôle : {@code admin}, {@code manager}, {@code operator}, {@code user}
 *   (mot de passe : {@code password}) ;</li>
 *   <li>5 chauffeurs dont 1 avec permis expiré et 1 suspendu (pour tester les blocages),
 *   5 véhicules dont 1 en maintenance, 3 clients ;</li>
 *   <li>quelques courses à différents statuts pour alimenter les statistiques.</li>
 * </ul>
 * Chaque bloc est idempotent : rien n'est créé si les tables sont déjà peuplées.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ParametreTarifRepository parametreTarifRepository;
    private final CompteUtilisateurRepository compteRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ClientRepository clientRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedParametresTarif();
        seedFlotte();
        seedComptes();
        seedClients();
        seedCourses();
    }

    private void seedParametresTarif() {
        if (parametreTarifRepository.count() > 0) {
            return;
        }
        parametreTarifRepository.save(ParametreTarif.builder()
                .tarifBase(new BigDecimal("2.50"))
                .prixKm(new BigDecimal("1.80"))
                .commissionPourcentage(new BigDecimal("20.00"))
                .build());
    }

    private void seedFlotte() {
        if (chauffeurRepository.count() > 0) {
            return;
        }
        Chauffeur kabasele = chauffeurRepository.save(Chauffeur.builder()
                .nom("Kabasele").prenom("Jonathan")
                .telephone("+243970000001")
                .numeroPermis("PERM-001")
                .dateExpirationPermis(LocalDate.now().plusYears(3))
                .statut(ChauffeurStatut.DISPONIBLE)
                .dateEmbauche(LocalDate.now().minusYears(4))
                .build());
        Chauffeur mbala = chauffeurRepository.save(Chauffeur.builder()
                .nom("Mbala").prenom("Grâce")
                .telephone("+243970000002")
                .numeroPermis("PERM-002")
                .dateExpirationPermis(LocalDate.now().plusYears(2))
                .statut(ChauffeurStatut.DISPONIBLE)
                .dateEmbauche(LocalDate.now().minusYears(2))
                .build());
        Chauffeur ilunga = chauffeurRepository.save(Chauffeur.builder()
                .nom("Ilunga").prenom("Patrick")
                .telephone("+243970000003")
                .numeroPermis("PERM-003")
                .dateExpirationPermis(LocalDate.now().plusMonths(6))
                .statut(ChauffeurStatut.DISPONIBLE)
                .dateEmbauche(LocalDate.now().minusYears(1))
                .build());
        chauffeurRepository.save(Chauffeur.builder()
                .nom("Tshibangu").prenom("Alain")
                .telephone("+243970000004")
                .numeroPermis("PERM-004-EXPIRE")
                .dateExpirationPermis(LocalDate.now().minusDays(30)) // permis expiré : doit être bloqué
                .statut(ChauffeurStatut.DISPONIBLE)
                .dateEmbauche(LocalDate.now().minusYears(3))
                .build());
        chauffeurRepository.save(Chauffeur.builder()
                .nom("Nsimba").prenom("Chantal")
                .telephone("+243970000005")
                .numeroPermis("PERM-005")
                .dateExpirationPermis(LocalDate.now().plusYears(1))
                .statut(ChauffeurStatut.SUSPENDU) // suspendu : doit être bloqué
                .dateEmbauche(LocalDate.now().minusMonths(8))
                .build());

        vehiculeRepository.save(Vehicule.builder()
                .immatriculation("CD-001-AB").marque("Toyota").modele("Corolla")
                .annee(2022).capacite(4).kilometrage(84500d)
                .statut(VehiculeStatut.DISPONIBLE)
                .chauffeurActuel(kabasele)
                .build());
        vehiculeRepository.save(Vehicule.builder()
                .immatriculation("CD-002-CD").marque("Hyundai").modele("i10")
                .annee(2021).capacite(4).kilometrage(62300d)
                .statut(VehiculeStatut.DISPONIBLE)
                .chauffeurActuel(mbala)
                .build());
        vehiculeRepository.save(Vehicule.builder()
                .immatriculation("CD-003-EF").marque("Renault").modele("Logan")
                .annee(2020).capacite(5).kilometrage(110700d)
                .statut(VehiculeStatut.DISPONIBLE)
                .chauffeurActuel(ilunga)
                .build());
        vehiculeRepository.save(Vehicule.builder()
                .immatriculation("CD-004-GH").marque("Toyota").modele("Hiace")
                .annee(2019).capacite(9).kilometrage(189000d)
                .statut(VehiculeStatut.MAINTENANCE) // en maintenance : doit être exclu du dispatch
                .build());
        vehiculeRepository.save(Vehicule.builder()
                .immatriculation("CD-005-IJ").marque("Suzuki").modele("Swift")
                .annee(2023).capacite(4).kilometrage(21400d)
                .statut(VehiculeStatut.DISPONIBLE)
                .build());
    }

    private void seedComptes() {
        if (compteRepository.count() > 0) {
            return;
        }
        String motDePasse = passwordEncoder.encode("password");
        compteRepository.save(CompteUtilisateur.builder()
                .username("admin").motDePasse(motDePasse).role(Role.ADMIN).build());
        compteRepository.save(CompteUtilisateur.builder()
                .username("manager").motDePasse(motDePasse).role(Role.MANAGER).build());
        compteRepository.save(CompteUtilisateur.builder()
                .username("operator").motDePasse(motDePasse).role(Role.OPERATOR).build());
        compteRepository.save(CompteUtilisateur.builder()
                .username("user").motDePasse(motDePasse).role(Role.USER).build());

        chauffeurRepository.findAll().stream()
                .filter(ch -> "PERM-001".equals(ch.getNumeroPermis()))
                .findFirst()
                .ifPresent(chauffeur -> compteRepository.save(CompteUtilisateur.builder()
                        .username("chauffeur1").motDePasse(motDePasse)
                        .role(Role.USER).chauffeur(chauffeur).build()));
    }

    private void seedClients() {
        if (clientRepository.count() > 0) {
            return;
        }
        clientRepository.save(Client.builder()
                .nom("Mulumba").prenom("Sarah").telephone("+243980000001")
                .email("sarah.mulumba@example.com").build());
        clientRepository.save(Client.builder()
                .nom("Bofenda").prenom("Dieudonné").telephone("+243980000002")
                .email("d.bofenda@example.com").build());
        clientRepository.save(Client.builder()
                .nom("Kalonji").prenom("Aimée").telephone("+243980000003")
                .email("aimee.kalonji@example.com").build());
    }

    private void seedCourses() {
        if (courseRepository.count() > 0) {
            return;
        }
        List<Client> clients = clientRepository.findAll();
        List<Chauffeur> chauffeurs = chauffeurRepository.findAll();
        List<Vehicule> vehicules = vehiculeRepository.findAll();
        if (clients.isEmpty() || chauffeurs.size() < 3 || vehicules.size() < 3) {
            return;
        }
        ParametreTarif tarif = parametreTarifRepository.findAll().stream().findFirst().orElse(null);
        if (tarif == null) {
            return;
        }

        // Trois courses terminées hier (alimentent le CA et les statistiques)
        enregistrerCourseTerminee(clients.get(0), chauffeurs.get(0), vehicules.get(0),
                "Gare de Kinshasa", "Boulevard du 30 Juin", 7.5, LocalDateTime.now().minusDays(1).withHour(9), tarif);
        enregistrerCourseTerminee(clients.get(1), chauffeurs.get(1), vehicules.get(1),
                "Aéroport de N'djili", "Hôtel Fleuve", 32.0, LocalDateTime.now().minusDays(1).withHour(14), tarif);
        enregistrerCourseTerminee(clients.get(2), chauffeurs.get(2), vehicules.get(2),
                "Marché Central", "Université de Kinshasa", 12.0, LocalDateTime.now().minusDays(1).withHour(18), tarif);

        // Une course annulée et une course en attente (alimentent le taux d'annulation et le dispatch)
        courseRepository.save(Course.builder()
                .client(clients.get(0))
                .pointDepart("Limete").destination("Ngaliema")
                .distanceKm(9.0).dateHeure(LocalDateTime.now().minusHours(3))
                .tarif(calculerTarif(9.0, tarif)).commission(calculerCommission(9.0, tarif))
                .montantChauffeur(calculerMontantChauffeur(9.0, tarif))
                .statut(CourseStatut.ANNULEE)
                .build());
        courseRepository.save(Course.builder()
                .client(clients.get(1))
                .pointDepart("Gombe, Avenue Kasa-Vubu").destination("Lingwala, Marché Gambela")
                .distanceKm(6.2).dateHeure(LocalDateTime.now().plusMinutes(15))
                .tarif(calculerTarif(6.2, tarif)).commission(calculerCommission(6.2, tarif))
                .montantChauffeur(calculerMontantChauffeur(6.2, tarif))
                .statut(CourseStatut.EN_ATTENTE)
                .build());
    }

    private void enregistrerCourseTerminee(Client client, Chauffeur chauffeur, Vehicule vehicule,
                                           String depart, String destination, double distanceKm,
                                           LocalDateTime dateHeure, ParametreTarif tarif) {
        courseRepository.save(Course.builder()
                .client(client)
                .pointDepart(depart).destination(destination)
                .distanceKm(distanceKm).dateHeure(dateHeure)
                .tarif(calculerTarif(distanceKm, tarif))
                .commission(calculerCommission(distanceKm, tarif))
                .montantChauffeur(calculerMontantChauffeur(distanceKm, tarif))
                .chauffeur(chauffeur)
                .vehicule(vehicule)
                .statut(CourseStatut.TERMINEE)
                .build());
    }

    private BigDecimal calculerTarif(double distanceKm, ParametreTarif tarif) {
        return tarif.getTarifBase()
                .add(tarif.getPrixKm().multiply(BigDecimal.valueOf(distanceKm)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculerCommission(double distanceKm, ParametreTarif tarif) {
        return calculerTarif(distanceKm, tarif)
                .multiply(tarif.getCommissionPourcentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculerMontantChauffeur(double distanceKm, ParametreTarif tarif) {
        return calculerTarif(distanceKm, tarif).subtract(calculerCommission(distanceKm, tarif));
    }
}
