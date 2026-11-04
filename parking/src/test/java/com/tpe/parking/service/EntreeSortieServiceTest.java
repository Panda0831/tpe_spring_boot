package com.tpe.parking.service;

import com.tpe.parking.dto.request.EntreeVehiculeRequest;
import com.tpe.parking.dto.request.SortieVehiculeRequest;
import com.tpe.parking.dto.response.EntreeSortieResponse;
import com.tpe.parking.entity.EntreeSortie;
import com.tpe.parking.entity.Place;
import com.tpe.parking.entity.PlaceStatut;
import com.tpe.parking.entity.PlaceType;
import com.tpe.parking.entity.Reservation;
import com.tpe.parking.entity.ReservationStatus;
import com.tpe.parking.entity.Role;
import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.entity.Vehicule;
import com.tpe.parking.exception.InvalidOperationException;
import com.tpe.parking.exception.PlaceUnavailableException;
import com.tpe.parking.exception.ResourceNotFoundException;
import com.tpe.parking.repository.EntreeSortieRepository;
import com.tpe.parking.repository.PlaceRepository;
import com.tpe.parking.repository.ReservationRepository;
import com.tpe.parking.repository.VehiculeRepository;
import com.tpe.parking.service.impl.EntreeSortieServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Entrées/sorties : attribution automatique de place (réservation, abonnement, substitution),
 * passage de statut LIBRE &lt;-&gt; OCCUPEE et facturation de la sortie.
 */
@ExtendWith(MockitoExtension.class)
class EntreeSortieServiceTest {

    @Mock
    private EntreeSortieRepository entreeSortieRepository;
    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private AbonnementService abonnementService;
    @Mock
    private TarifService tarifService;

    @InjectMocks
    private EntreeSortieServiceImpl entreeSortieService;

    private Utilisateur proprietaire;
    private Vehicule vehicule;

    @BeforeEach
    void initialiser() {
        proprietaire = new Utilisateur(4L, "Kasongo", "Céline", "+243900000004",
                "user@parking.com", "hash", Role.USER);
        vehicule = new Vehicule(1L, "CD-AAA-001", "Toyota", "Yaris", proprietaire);
    }

    /** Retourne l'entité transmise, comme le ferait la couche de persistance. */
    private void avecPersistance() {
        when(entreeSortieRepository.save(any(EntreeSortie.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Entrée : la place réservée par le client est attribuée en priorité")
    void entreeUtiliseLaReservationEnCours() {
        avecPersistance();
        Place placeReservee = new Place(6L, "A6", "RDC", PlaceType.STANDARD, PlaceStatut.LIBRE);
        LocalDateTime arrivee = LocalDateTime.now().minusMinutes(5);
        LocalDateTime depart = arrivee.plusHours(3);
        Reservation reservation = new Reservation(1L, proprietaire, vehicule, placeReservee,
                arrivee, depart, ReservationStatus.CONFIRMEE);

        when(vehiculeRepository.findByImmatriculation("CD-AAA-001")).thenReturn(Optional.of(vehicule));
        when(entreeSortieRepository.findEntreeEnCoursParImmatriculation("CD-AAA-001")).thenReturn(Optional.empty());
        when(reservationRepository.findByVehiculeId(1L)).thenReturn(List.of(reservation));

        EntreeSortieResponse ticket = entreeSortieService.enregistrerEntree(
                new EntreeVehiculeRequest("cd-aaa-001", null, null));

        assertThat(ticket.numeroPlace()).isEqualTo("A6");
        assertThat(ticket.dateDepartPrevue()).isEqualTo(depart);
        assertThat(placeReservee.getStatut()).isEqualTo(PlaceStatut.OCCUPEE);
        assertThat(ticket.enCours()).isTrue();
        verify(placeRepository, never()).findByStatut(any());
    }

    @Test
    @DisplayName("Entrée d'un abonné : une place ABONNE libre est choisie automatiquement")
    void entreeAbonneChoisitPlaceAbonne() {
        avecPersistance();
        Place placeStandard = new Place(4L, "A4", "RDC", PlaceType.STANDARD, PlaceStatut.LIBRE);
        Place placeAbonne = new Place(3L, "A3", "RDC", PlaceType.ABONNE, PlaceStatut.LIBRE);

        when(vehiculeRepository.findByImmatriculation("CD-AAA-001")).thenReturn(Optional.of(vehicule));
        when(entreeSortieRepository.findEntreeEnCoursParImmatriculation(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.findByVehiculeId(anyLong())).thenReturn(List.of());
        when(placeRepository.findByStatut(PlaceStatut.LIBRE)).thenReturn(List.of(placeStandard, placeAbonne));
        when(abonnementService.estAbonneActif(1L)).thenReturn(true);

        EntreeSortieResponse ticket = entreeSortieService.enregistrerEntree(
                new EntreeVehiculeRequest("CD-AAA-001", null, null));

        assertThat(ticket.numeroPlace()).isEqualTo("A3");
        assertThat(placeAbonne.getStatut()).isEqualTo(PlaceStatut.OCCUPEE);
        assertThat(placeStandard.getStatut()).isEqualTo(PlaceStatut.LIBRE);
    }

    @Test
    @DisplayName("Entrée : véhicule inconnu => ResourceNotFoundException")
    void entreeVehiculeInconnu() {
        when(vehiculeRepository.findByImmatriculation("XX-000-XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entreeSortieService.enregistrerEntree(
                new EntreeVehiculeRequest("XX-000-XX", null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("XX-000-XX");
    }

    @Test
    @DisplayName("Entrée : un véhicule déjà présent est refusé")
    void entreeVehiculeDejaPresent() {
        when(vehiculeRepository.findByImmatriculation("CD-AAA-001")).thenReturn(Optional.of(vehicule));
        when(entreeSortieRepository.findEntreeEnCoursParImmatriculation("CD-AAA-001"))
                .thenReturn(Optional.of(new EntreeSortie(10L, vehicule, null, LocalDateTime.now(), null, null)));

        assertThatThrownBy(() -> entreeSortieService.enregistrerEntree(
                new EntreeVehiculeRequest("CD-AAA-001", null, null)))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("déjà présent");
    }

    @Test
    @DisplayName("Entrée : aucune place libre => PlaceUnavailableException")
    void entreeParkingComplet() {
        when(vehiculeRepository.findByImmatriculation("CD-AAA-001")).thenReturn(Optional.of(vehicule));
        when(entreeSortieRepository.findEntreeEnCoursParImmatriculation(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.findByVehiculeId(anyLong())).thenReturn(List.of());
        when(placeRepository.findByStatut(PlaceStatut.LIBRE)).thenReturn(List.of());

        assertThatThrownBy(() -> entreeSortieService.enregistrerEntree(
                new EntreeVehiculeRequest("CD-AAA-001", null, null)))
                .isInstanceOf(PlaceUnavailableException.class);
    }

    @Test
    @DisplayName("Sortie : la place est libérée et la sortie facturée")
    void sortieFactureEtLibereLaPlace() {
        avecPersistance();
        Place place = new Place(5L, "A5", "RDC", PlaceType.STANDARD, PlaceStatut.OCCUPEE);
        EntreeSortie entree = new EntreeSortie(7L, vehicule, place,
                LocalDateTime.now().minusHours(3), null, null);

        when(vehiculeRepository.findByImmatriculation("CD-AAA-001")).thenReturn(Optional.of(vehicule));
        when(entreeSortieRepository.findEntreeEnCoursParImmatriculation("CD-AAA-001")).thenReturn(Optional.of(entree));
        when(abonnementService.estAbonneActif(1L)).thenReturn(false);
        when(tarifService.calculerMontantAvecDepassement(any(), any(), any(), eq("RDC"), anyBoolean(), eq(Role.USER), any()))
                .thenReturn(new BigDecimal("7.50"));

        EntreeSortieResponse ticket = entreeSortieService.enregistrerSortie(
                new SortieVehiculeRequest("CD-AAA-001"));

        assertThat(ticket.montant()).isEqualByComparingTo("7.50");
        assertThat(ticket.enCours()).isFalse();
        assertThat(place.getStatut()).isEqualTo(PlaceStatut.LIBRE);
    }

    @Test
    @DisplayName("Sortie sans entrée en cours => InvalidOperationException")
    void sortieSansEntree() {
        when(vehiculeRepository.findByImmatriculation("CD-AAA-001")).thenReturn(Optional.of(vehicule));
        when(entreeSortieRepository.findEntreeEnCoursParImmatriculation("CD-AAA-001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entreeSortieService.enregistrerSortie(
                new SortieVehiculeRequest("CD-AAA-001")))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Aucune entrée en cours");
    }
}
