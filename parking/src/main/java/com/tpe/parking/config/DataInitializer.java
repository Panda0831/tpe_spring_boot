package com.tpe.parking.config;

import com.tpe.parking.entity.Abonnement;
import com.tpe.parking.entity.AbonnementStatut;
import com.tpe.parking.entity.AbonnementType;
import com.tpe.parking.entity.EntreeSortie;
import com.tpe.parking.entity.Place;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.PlaceType;
import com.tpe.parking.entity.Reservation;
import com.tpe.parking.entity.ReservationStatus;
import com.tpe.parking.entity.Role;
import com.tpe.parking.entity.Tarif;
import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.entity.Vehicule;
import com.tpe.parking.repository.AbonnementRepository;
import com.tpe.parking.repository.EntreeSortieRepository;
import com.tpe.parking.repository.PlaceRepository;
import com.tpe.parking.repository.ReservationRepository;
import com.tpe.parking.repository.TarifRepository;
import com.tpe.parking.repository.UtilisateurRepository;
import com.tpe.parking.repository.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Jeu de données de démonstration (exigence 3.3 du plan) :
 * <ul>
 *   <li>un compte par rôle : {@code admin@parking.com}, {@code manager@parking.com},
 *   {@code operator@parking.com}, {@code user@parking.com} (mot de passe : {@code password}) ;</li>
 *   <li>24 places réparties en 2 zones avec les 4 types (STANDARD, PMR, VIP, ABONNE),
 *   2 tarifs, 4 utilisateurs, 4 véhicules, abonnements actif + expiré ;</li>
 *   <li>une entrée en cours (place occupée), une réservation à venir et un historique
 *   d'entrée/sortie facturé (dépassement).</li>
 * </ul>
 * Chaque bloc est idempotent : rien n'est créé si les tables sont déjà peuplées.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final PlaceRepository placeRepository;
    private final AbonnementRepository abonnementRepository;
    private final ReservationRepository reservationRepository;
    private final EntreeSortieRepository entreeSortieRepository;
    private final TarifRepository tarifRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedTarifs();
        seedPlaces();
        seedUtilisateursEtVehicules();
        seedAbonnements();
        seedReservationsEtEntreesSorties();
    }

    private void seedTarifs() {
        if (tarifRepository.count() > 0) {
            return;
        }
        tarifRepository.save(new Tarif(null, "RDC",
                new BigDecimal("2.00"),   // montant par heure
                new BigDecimal("1.50"),   // tarif minimum
                new BigDecimal("15.00"),  // forfait journalier
                new BigDecimal("0.20"),   // réduction abonné (20%)
                new BigDecimal("0.50"))); // majoration après dépassement (+50%)
        tarifRepository.save(new Tarif(null, "Etage1",
                new BigDecimal("1.50"),
                new BigDecimal("1.00"),
                new BigDecimal("10.00"),
                new BigDecimal("0.20"),
                new BigDecimal("0.50")));
    }

    private void seedPlaces() {
        if (placeRepository.count() > 0) {
            return;
        }
        // Zone RDC : A1..A12 (A1 PMR, A2 VIP, A3 ABONNE, le reste STANDARD)
        for (int i = 1; i <= 12; i++) {
            PlaceType type = switch (i) {
                case 1 -> PlaceType.PMR;
                case 2 -> PlaceType.VIP;
                case 3 -> PlaceType.ABONNE;
                default -> PlaceType.STANDARD;
            };
            placeRepository.save(new Place(null, "A" + i, "RDC", type, PlaceStatut.LIBRE));
        }
        // Zone Etage1 : B1..B12 (B1 VIP, B2 PMR, le reste STANDARD)
        for (int i = 1; i <= 12; i++) {
            PlaceType type = switch (i) {
                case 1 -> PlaceType.VIP;
                case 2 -> PlaceType.PMR;
                default -> PlaceType.STANDARD;
            };
            placeRepository.save(new Place(null, "B" + i, "Etage1", type, PlaceStatut.LIBRE));
        }
    }

    private void seedUtilisateursEtVehicules() {
        String motDePasse = passwordEncoder.encode("password");

        List<Utilisateur> comptesDemo = List.of(
                new Utilisateur(null, "Admin", "Système", "+243900000001",
                        "admin@parking.com", motDePasse, Role.ADMIN),
                new Utilisateur(null, "Mbuyi", "Josué", "+243900000002",
                        "manager@parking.com", motDePasse, Role.MANAGER),
                new Utilisateur(null, "Ngoy", "Baraka", "+243900000003",
                        "operator@parking.com", motDePasse, Role.OPERATOR),
                new Utilisateur(null, "Kasongo", "Céline", "+243900000004",
                        "user@parking.com", motDePasse, Role.USER),
                new Utilisateur(null, "Vip", "Client", "+243900000005",
                        "vip@parking.com", motDePasse, Role.USER));

        List<Utilisateur> comptesManquants = comptesDemo.stream()
                .filter(compte -> utilisateurRepository.findByEmail(compte.getEmail()).isEmpty())
                .toList();
        if (!comptesManquants.isEmpty()) {
            utilisateurRepository.saveAll(comptesManquants);
        }

        if (vehiculeRepository.count() > 0) {
            return;
        }
        Utilisateur user = utilisateurRepository.findByEmail("user@parking.com").orElse(null);
        Utilisateur vip = utilisateurRepository.findByEmail("vip@parking.com").orElse(null);
        if (user == null || vip == null) {
            return;
        }
        vehiculeRepository.save(new Vehicule(null, "CD-AAA-001", "Toyota", "Yaris", user));
        vehiculeRepository.save(new Vehicule(null, "CD-AAA-002", "Honda", "Jazz", user));
        vehiculeRepository.save(new Vehicule(null, "CD-AAA-003", "Mercedes", "Classe S", vip));
        vehiculeRepository.save(new Vehicule(null, "CD-AAA-004", "Renault", "Clio", null));
    }

    private void seedAbonnements() {
        if (abonnementRepository.count() > 0) {
            return;
        }
        Utilisateur user = utilisateurRepository.findByEmail("user@parking.com").orElse(null);
        Utilisateur vip = utilisateurRepository.findByEmail("vip@parking.com").orElse(null);
        Vehicule vehiculeUser = vehiculeRepository.findByImmatriculation("CD-AAA-001").orElse(null);
        Vehicule vehiculeVip = vehiculeRepository.findByImmatriculation("CD-AAA-003").orElse(null);
        if (user == null || vip == null || vehiculeUser == null || vehiculeVip == null) {
            return;
        }
        abonnementRepository.save(new Abonnement(null, user, vehiculeUser,
                AbonnementType.MENSUEL, LocalDate.now().minusDays(10), LocalDate.now().plusDays(20),
                AbonnementStatut.ACTIF));
        abonnementRepository.save(new Abonnement(null, vip, vehiculeVip,
                AbonnementType.ANNUEL, LocalDate.now().minusYears(1).minusDays(5), LocalDate.now().minusDays(5),
                AbonnementStatut.EXPIRE));
    }

    private void seedReservationsEtEntreesSorties() {
        if (reservationRepository.count() > 0 || entreeSortieRepository.count() > 0) {
            return;
        }
        Utilisateur user = utilisateurRepository.findByEmail("user@parking.com").orElse(null);
        Utilisateur vip = utilisateurRepository.findByEmail("vip@parking.com").orElse(null);
        Vehicule vehiculeUser = vehiculeRepository.findByImmatriculation("CD-AAA-001").orElse(null);
        Vehicule vehiculeVip = vehiculeRepository.findByImmatriculation("CD-AAA-003").orElse(null);
        Place placeOccupee = placeRepository.findByNumero("A5").orElse(null);
        Place placeReservee = placeRepository.findByNumero("A6").orElse(null);
        Place placeHisto = placeRepository.findByNumero("A7").orElse(null);
        if (user == null || vip == null || vehiculeUser == null || vehiculeVip == null
                || placeOccupee == null || placeReservee == null || placeHisto == null) {
            return;
        }

        // Entrée en cours : véhicule du client user sur A5 (place OCCUPEE)
        placeOccupee.setStatut(PlaceStatut.OCCUPEE);
        entreeSortieRepository.save(new EntreeSortie(null, vehiculeUser, placeOccupee,
                LocalDateTime.now().minusHours(2), null, null));

        // Réservation à venir sur A6 (place RESERVEE)
        placeReservee.setStatut(PlaceStatut.RESERVEE);
        reservationRepository.save(new Reservation(null, user, vehiculeUser, placeReservee,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(4),
                ReservationStatus.CONFIRMEE));

        // Historique facturé : entrée hier, sortie après la date prévue (dépassement facturé)
        LocalDateTime hierMatin = LocalDateTime.now().minusDays(1).withHour(8).withMinute(0);
        LocalDateTime departPrevu = hierMatin.plusHours(3);
        LocalDateTime sortieReelle = hierMatin.plusHours(5); // 2h de dépassement
        entreeSortieRepository.save(new EntreeSortie(null, vehiculeVip, placeHisto,
                hierMatin, sortieReelle, new BigDecimal("7.50")));
    }
}
