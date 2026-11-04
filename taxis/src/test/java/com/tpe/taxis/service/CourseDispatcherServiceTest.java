package com.tpe.taxis.service;

import com.tpe.taxis.dto.request.CreateCourseRequest;
import com.tpe.taxis.dto.response.CourseResponse;
import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.ChauffeurStatut;
import com.tpe.taxis.entity.Client;
import com.tpe.taxis.entity.Course;
import com.tpe.taxis.entity.CourseStatut;
import com.tpe.taxis.entity.ParametreTarif;
import com.tpe.taxis.entity.Vehicule;
import com.tpe.taxis.entity.VehiculeStatut;
import com.tpe.taxis.exception.ChauffeurIndisponibleException;
import com.tpe.taxis.exception.CourseConflictException;
import com.tpe.taxis.exception.VehiculeEnMaintenanceException;
import com.tpe.taxis.repository.ChauffeurRepository;
import com.tpe.taxis.repository.ClientRepository;
import com.tpe.taxis.repository.CourseRepository;
import com.tpe.taxis.repository.ParametreTarifRepository;
import com.tpe.taxis.repository.VehiculeRepository;
import com.tpe.taxis.service.impl.CourseDispatcherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Moteur d'attribution automatique : seuls les chauffeurs éligibles (statut DISPONIBLE,
 * permis valide, sans course active, avec véhicule exploitable) sont candidats ; le score
 * combine proximité et équité, avec départage déterministe par ancienneté d'embauche.
 */
@ExtendWith(MockitoExtension.class)
class CourseDispatcherServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ChauffeurRepository chauffeurRepository;
    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private ParametreTarifRepository parametreTarifRepository;

    @InjectMocks
    private CourseDispatcherServiceImpl dispatcher;

    private Client client;
    private ParametreTarif parametre;

    @BeforeEach
    void initialiser() {
        client = Client.builder().id(1L).nom("Mulumba").prenom("Sarah").telephone("+243980000001").build();
        parametre = ParametreTarif.builder()
                .tarifBase(new BigDecimal("2.50"))
                .prixKm(new BigDecimal("1.80"))
                .commissionPourcentage(new BigDecimal("20.00"))
                .poidsProximite(new BigDecimal("0.60"))
                .poidsEquite(new BigDecimal("0.40"))
                .build();
    }

    private Vehicule vehicule(Long id, String immatriculation, VehiculeStatut statut, Chauffeur chauffeur) {
        return Vehicule.builder()
                .id(id).immatriculation(immatriculation).marque("Toyota").modele("Corolla")
                .annee(2022).capacite(4).kilometrage(1000d).statut(statut).chauffeurActuel(chauffeur)
                .build();
    }

    private Chauffeur chauffeur(Long id, LocalDate embauche, LocalDate expirationPermis,
                                ChauffeurStatut statut, Vehicule vehicule) {
        return Chauffeur.builder()
                .id(id).nom("Nom" + id).prenom("Prenom" + id).telephone("+2439700000" + id)
                .numeroPermis("PERM-" + id).dateExpirationPermis(expirationPermis)
                .statut(statut).dateEmbauche(embauche).vehicule(vehicule)
                .build();
    }

    /** Aucun historique de course pour ce chauffeur. */
    private void sansHistorique(Long chauffeurId) {
        lenient().when(courseRepository.existsByChauffeurIdAndStatutIn(eq(chauffeurId), any()))
                .thenReturn(false);
        lenient().when(courseRepository.countByChauffeurIdAndDateHeureAfter(eq(chauffeurId), any()))
                .thenReturn(0L);
        lenient().when(courseRepository.findCoursesActivesByChauffeur(chauffeurId)).thenReturn(List.of());
        lenient().when(courseRepository.findByChauffeurIdAndStatutOrderByDateHeureDesc(chauffeurId, CourseStatut.TERMINEE))
                .thenReturn(List.of());
        lenient().when(vehiculeRepository.findByChauffeurActuelId(chauffeurId)).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Dispatch automatique : à scores égaux, le chauffeur le plus ancien est retenu")
    void dispatchChoisitLePlusAncienEnCasDEgalite() {
        Chauffeur ancien = chauffeur(1L, LocalDate.now().minusYears(4), LocalDate.now().plusYears(3),
                ChauffeurStatut.DISPONIBLE, null);
        Chauffeur recent = chauffeur(2L, LocalDate.now().minusYears(1), LocalDate.now().plusYears(3),
                ChauffeurStatut.DISPONIBLE, null);
        ancien.setVehicule(vehicule(10L, "CD-001-AB", VehiculeStatut.DISPONIBLE, ancien));
        recent.setVehicule(vehicule(20L, "CD-002-CD", VehiculeStatut.DISPONIBLE, recent));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(parametreTarifRepository.findAll()).thenReturn(List.of(parametre));
        when(chauffeurRepository.findEligiblesPourCourse(any(LocalDate.class))).thenReturn(List.of(recent, ancien));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        sansHistorique(1L);
        sansHistorique(2L);

        CourseResponse course = dispatcher.dispatcherCourse(
                new CreateCourseRequest(1L, "Gombe", "Limete", 5.0, null, null, null));

        assertThat(course.chauffeurId()).isEqualTo(1L);
        assertThat(course.immatriculationVehicule()).isEqualTo("CD-001-AB");
        assertThat(course.statut()).isEqualTo(CourseStatut.ASSIGNEE);
        // tarif = 2.50 + 1.80 x 5 = 11.50, commission 20 % = 2.30
        assertThat(course.tarif()).isEqualByComparingTo("11.50");
        assertThat(course.commission()).isEqualByComparingTo("2.30");
    }

    @Test
    @DisplayName("Un chauffeur dont le véhicule est en maintenance et sans véhicule de secours est écarté")
    void dispatchEcarteVehiculeEnMaintenance() {
        Chauffeur chauffeur = chauffeur(1L, LocalDate.now().minusYears(4), LocalDate.now().plusYears(3),
                ChauffeurStatut.DISPONIBLE, null);
        chauffeur.setVehicule(vehicule(10L, "CD-004-GH", VehiculeStatut.MAINTENANCE, chauffeur));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(parametreTarifRepository.findAll()).thenReturn(List.of(parametre));
        when(chauffeurRepository.findEligiblesPourCourse(any(LocalDate.class))).thenReturn(List.of(chauffeur));
        when(vehiculeRepository.findDisponiblesPourCourse()).thenReturn(List.of());
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        when(courseRepository.existsByChauffeurIdAndStatutIn(eq(1L), any())).thenReturn(false);

        CourseResponse course = dispatcher.dispatcherCourse(
                new CreateCourseRequest(1L, "Gombe", "Limete", 5.0, null, null, null));

        assertThat(course.statut()).isEqualTo(CourseStatut.EN_ATTENTE);
        assertThat(course.chauffeurId()).isNull();
    }

    @Test
    @DisplayName("Un chauffeur ayant déjà une course active n'est pas re-dispatché")
    void dispatchEcarteChauffeurAvecCourseActive() {
        Chauffeur chauffeur = chauffeur(1L, LocalDate.now().minusYears(4), LocalDate.now().plusYears(3),
                ChauffeurStatut.DISPONIBLE, null);
        chauffeur.setVehicule(vehicule(10L, "CD-001-AB", VehiculeStatut.DISPONIBLE, chauffeur));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(parametreTarifRepository.findAll()).thenReturn(List.of(parametre));
        when(chauffeurRepository.findEligiblesPourCourse(any(LocalDate.class))).thenReturn(List.of(chauffeur));
        when(courseRepository.existsByChauffeurIdAndStatutIn(eq(1L), any())).thenReturn(true);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse course = dispatcher.dispatcherCourse(
                new CreateCourseRequest(1L, "Gombe", "Limete", 5.0, null, null, null));

        assertThat(course.statut()).isEqualTo(CourseStatut.EN_ATTENTE);
        assertThat(course.chauffeurId()).isNull();
    }

    @Test
    @DisplayName("Seule une course EN_ATTENTE peut être dispatchée")
    void dispatchRefuseCourseDejaAssignee() {
        Course course = Course.builder().id(7L).client(client).pointDepart("A").destination("B")
                .distanceKm(5.0).dateHeure(LocalDateTime.now()).statut(CourseStatut.TERMINEE).build();
        when(courseRepository.findById(7L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> dispatcher.dispatcherCourse(7L))
                .isInstanceOf(CourseConflictException.class)
                .hasMessageContaining("EN_ATTENTE");
    }

    @Test
    @DisplayName("Affectation manuelle : un chauffeur avec permis expiré est refusé")
    void affectationManuelleRefusePermisExpire() {
        Course course = Course.builder().id(7L).client(client).pointDepart("A").destination("B")
                .distanceKm(5.0).dateHeure(LocalDateTime.now()).statut(CourseStatut.EN_ATTENTE).build();
        Chauffeur expire = chauffeur(3L, LocalDate.now().minusYears(3), LocalDate.now().minusDays(30),
                ChauffeurStatut.DISPONIBLE, null);

        when(courseRepository.findById(7L)).thenReturn(Optional.of(course));
        when(chauffeurRepository.findById(3L)).thenReturn(Optional.of(expire));

        assertThatThrownBy(() -> dispatcher.assignerManuellement(7L, 3L, null))
                .isInstanceOf(ChauffeurIndisponibleException.class);
    }

    @Test
    @DisplayName("Affectation manuelle : un chauffeur suspendu est refusé")
    void affectationManuelleRefuseChauffeurSuspendu() {
        Course course = Course.builder().id(7L).client(client).pointDepart("A").destination("B")
                .distanceKm(5.0).dateHeure(LocalDateTime.now()).statut(CourseStatut.EN_ATTENTE).build();
        Chauffeur suspendu = chauffeur(5L, LocalDate.now().minusMonths(8), LocalDate.now().plusYears(1),
                ChauffeurStatut.SUSPENDU, null);

        when(courseRepository.findById(7L)).thenReturn(Optional.of(course));
        when(chauffeurRepository.findById(5L)).thenReturn(Optional.of(suspendu));

        assertThatThrownBy(() -> dispatcher.assignerManuellement(7L, 5L, null))
                .isInstanceOf(ChauffeurIndisponibleException.class);
    }

    @Test
    @DisplayName("Affectation manuelle : un véhicule en maintenance est refusé")
    void affectationManuelleRefuseVehiculeEnMaintenance() {
        Course course = Course.builder().id(7L).client(client).pointDepart("A").destination("B")
                .distanceKm(5.0).dateHeure(LocalDateTime.now()).statut(CourseStatut.EN_ATTENTE).build();
        Chauffeur chauffeur = chauffeur(1L, LocalDate.now().minusYears(4), LocalDate.now().plusYears(3),
                ChauffeurStatut.DISPONIBLE, null);
        Vehicule maintenance = vehicule(4L, "CD-004-GH", VehiculeStatut.MAINTENANCE, null);

        when(courseRepository.findById(7L)).thenReturn(Optional.of(course));
        when(chauffeurRepository.findById(1L)).thenReturn(Optional.of(chauffeur));
        when(vehiculeRepository.findById(4L)).thenReturn(Optional.of(maintenance));

        assertThatThrownBy(() -> dispatcher.assignerManuellement(7L, 1L, 4L))
                .isInstanceOf(VehiculeEnMaintenanceException.class);
    }

    @Test
    @DisplayName("Affectation manuelle valide : la course passe en ASSIGNEE")
    void affectationManuelleValide() {
        Course course = Course.builder().id(7L).client(client).pointDepart("A").destination("B")
                .distanceKm(5.0).dateHeure(LocalDateTime.now()).statut(CourseStatut.EN_ATTENTE).build();
        Chauffeur chauffeur = chauffeur(1L, LocalDate.now().minusYears(4), LocalDate.now().plusYears(3),
                ChauffeurStatut.DISPONIBLE, null);
        Vehicule vehicule = vehicule(1L, "CD-001-AB", VehiculeStatut.DISPONIBLE, chauffeur);

        when(courseRepository.findById(7L)).thenReturn(Optional.of(course));
        when(chauffeurRepository.findById(1L)).thenReturn(Optional.of(chauffeur));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        when(vehiculeRepository.findByChauffeurActuelId(anyLong())).thenReturn(Optional.of(vehicule));

        CourseResponse response = dispatcher.assignerManuellement(7L, 1L, 1L);

        assertThat(response.statut()).isEqualTo(CourseStatut.ASSIGNEE);
        assertThat(response.nomChauffeur()).contains("Nom1");
        assertThat(response.immatriculationVehicule()).isEqualTo("CD-001-AB");
    }
}
