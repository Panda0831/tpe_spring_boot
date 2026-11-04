package com.tpe.taxis.service.impl;

import com.tpe.taxis.dto.request.CreateCourseRequest;
import com.tpe.taxis.dto.response.CourseResponse;
import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.Course;
import com.tpe.taxis.entity.CourseStatut;
import com.tpe.taxis.entity.ParametreTarif;
import com.tpe.taxis.entity.Vehicule;
import com.tpe.taxis.entity.VehiculeStatut;
import com.tpe.taxis.exception.BusinessException;
import com.tpe.taxis.exception.CourseConflictException;
import com.tpe.taxis.exception.ResourceNotFoundException;
import com.tpe.taxis.mapper.CourseMapper;
import com.tpe.taxis.repository.ChauffeurRepository;
import com.tpe.taxis.repository.ClientRepository;
import com.tpe.taxis.repository.CourseRepository;
import com.tpe.taxis.repository.ParametreTarifRepository;
import com.tpe.taxis.repository.VehiculeRepository;
import com.tpe.taxis.service.CourseDispatcherService;
import com.tpe.taxis.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseDispatcherServiceImpl implements CourseDispatcherService {

    /** Fenêtre glissante (en jours) prise en compte pour le critère d'équité. */
    private static final int FENETRE_EQUITE_JOURS = 30;

    /** Rayon (km) au-delà duquel la proximité ne rapporte plus aucun point. */
    private static final double DISTANCE_MAX_PROXIMITE_KM = 15.0;

    /** Ce score est purement informatif : on le borne à 4 décimales pour les comparaisons. */
    private static final int SCALE_SCORE = 4;

    private final CourseRepository courseRepository;
    private final ClientRepository clientRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ParametreTarifRepository parametreTarifRepository;

    @Override
    public CourseResponse dispatcherCourse(CreateCourseRequest request) {
        var client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.clientId()));

        BigDecimal[] montants = calculerTarifs(request.distanceKm());
        Course course = CourseMapper.toEntity(request, client, null, null,
                montants[0], montants[1], montants[2], CourseStatut.EN_ATTENTE);

        Optional<Candidat> meilleur = trouverMeilleurCandidat(course);
        if (meilleur.isPresent()) {
            Candidat candidat = meilleur.get();
            course.setChauffeur(candidat.chauffeur());
            course.setVehicule(candidat.vehicule());
            course.setStatut(CourseStatut.ASSIGNEE);
        }

        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse dispatcherCourse(Long courseId) {
        Course course = getCourseEntity(courseId);
        if (course.getStatut() != CourseStatut.EN_ATTENTE) {
            throw new CourseConflictException(course.getId(),
                    "seule une course EN_ATTENTE peut être dispatchée (statut actuel : " + course.getStatut() + ")");
        }

        Optional<Candidat> meilleur = trouverMeilleurCandidat(course);
        meilleur.ifPresent(candidat -> affecter(course, candidat));

        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse assignerManuellement(Long courseId, Long chauffeurId, Long vehiculeId) {
        Course course = getCourseEntity(courseId);
        if (course.getStatut() != CourseStatut.EN_ATTENTE) {
            throw new CourseConflictException(course.getId(),
                    "seule une course EN_ATTENTE peut être assignée (statut actuel : " + course.getStatut() + ")");
        }

        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new ResourceNotFoundException("Chauffeur", chauffeurId));
        if (!chauffeur.isEligiblePourCourse()) {
            throw new com.tpe.taxis.exception.ChauffeurIndisponibleException(
                    chauffeur.getId(), chauffeur.getStatut());
        }

        Vehicule vehicule;
        if (vehiculeId != null) {
            vehicule = vehiculeRepository.findById(vehiculeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicule", vehiculeId));
            if (vehicule.getStatut() != VehiculeStatut.DISPONIBLE) {
                throw new com.tpe.taxis.exception.VehiculeEnMaintenanceException(
                        vehicule.getId(), vehicule.getImmatriculation(), vehicule.getStatut());
            }
        } else {
            vehicule = chauffeur.getVehicule();
        }
        if (vehicule == null) {
            throw new BusinessException(
                    "Le chauffeur (id: " + chauffeurId + ") n'a aucun véhicule affecté.");
        }

        affecter(course, new Candidat(chauffeur, vehicule, null));
        return CourseMapper.toResponse(courseRepository.save(course));
    }

    /**
     * Moteur d'attribution : parmi les chauffeurs éligibles (statut DISPONIBLE, permis valide,
     * sans course active et avec un véhicule exploitable), calcule un score de pertinence
     * et retient le meilleur.
     *
     * <p>Le score combine deux critères normalisés sur [0, 1], pondérés par des coefficients
     * configurables via {@link ParametreTarif} :</p>
     * <ul>
     *   <li><b>Proximité</b> : distance estimée entre la dernière destination connue du chauffeur
     *   et le point de départ de la course (proche =&gt; score élevé) ;</li>
     *   <li><b>Équité</b> : rareté du chauffeur, c'est-à-dire l'inverse du nombre de courses
     *   qui lui ont été affectées au cours de la fenêtre glissante
     *   (peu sollicité =&gt; score élevé).</li>
     * </ul>
     *
     * <p>En cas d'égalité parfaite de scores, le chauffeur embauché depuis le plus longtemps
     * est privilégié, pour garantir un résultat déterministe.</p>
     */
    private Optional<Candidat> trouverMeilleurCandidat(Course course) {
        List<Chauffeur> chauffeurs = chauffeurRepository.findEligiblesPourCourse(LocalDateTime.now().toLocalDate());
        if (chauffeurs.isEmpty()) {
            return Optional.empty();
        }

        ParametreTarif parametre = parametreTarifRepository.findAll().stream()
                .reduce((first, second) -> second)
                .orElse(null);
        BigDecimal poidsProximite = poidsProximite(parametre);
        BigDecimal poidsEquite = poidsEquite(parametre);

        LocalDateTime debutFenetre = LocalDateTime.now().minusDays(FENETRE_EQUITE_JOURS);
        // Normalisateur du critère d'équité : maximum de courses récentes parmi les candidats.
        long coursesRecentesMax = chauffeurs.stream()
                .mapToLong(ch -> courseRepository.countByChauffeurIdAndDateHeureAfter(ch.getId(), debutFenetre))
                .max()
                .orElse(0L);

        return chauffeurs.stream()
                .map(chauffeur -> construireCandidat(chauffeur, course, poidsProximite, poidsEquite, coursesRecentesMax))
                .flatMap(Optional::stream)
                .max(Comparator
                        .comparing(Candidat::score)
                        // Départage déterministe : à score égal, le chauffeur embauché depuis
                        // le plus longtemps l'emporte (dates décroissantes => le max est le plus ancien).
                        .thenComparing(c -> c.chauffeur().getDateEmbauche(),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                ;
    }

    /**
     * Construit le couple (chauffeur, véhicule) exploitable pour ce chauffeur et calcule son score.
     * Retourne {@link Optional#empty()} si le chauffeur n'est pas mobilisable :
     * course active en cours ou aucun véhicule disponible associé / de secours.
     */
    private Optional<Candidat> construireCandidat(Chauffeur chauffeur, Course course,
                                                  BigDecimal poidsProximite, BigDecimal poidsEquite,
                                                  long coursesRecentesMax) {
        boolean sansCourseActive = !courseRepository.existsByChauffeurIdAndStatutIn(
                chauffeur.getId(), List.of(CourseStatut.ASSIGNEE, CourseStatut.EN_COURS));
        if (!sansCourseActive) {
            return Optional.empty();
        }

        Vehicule vehicule = vehiculePour(chauffeur);
        if (vehicule == null) {
            return Optional.empty();
        }

        BigDecimal score = calculerScore(chauffeur, course, poidsProximite, poidsEquite, coursesRecentesMax);
        return Optional.of(new Candidat(chauffeur, vehicule, score));
    }

    /**
     * Véhicule mobilisable pour le chauffeur : son véhicule s'il est disponible,
     * sinon un véhicule disponible non affecté à un autre chauffeur (fallback).
     */
    private Vehicule vehiculePour(Chauffeur chauffeur) {
        Vehicule vehicule = chauffeur.getVehicule();
        if (vehicule == null || vehicule.getStatut() != VehiculeStatut.DISPONIBLE) {
            return vehiculeRepository.findDisponiblesPourCourse().stream()
                    .filter(v -> v.getChauffeurActuel() == null
                            || v.getChauffeurActuel().getId().equals(chauffeur.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return vehicule;
    }

    /**
     * Score de pertinence = {@code poidsProximite × proximité + poidsEquite × équité}, arrondi à 4 décimales.
     *
     * @param coursesRecentesMax nombre maximal de courses récentes parmi les candidats,
     *                           servant de normalisateur du critère d'équité (jamais 0 ici)
     */
    private BigDecimal calculerScore(Chauffeur chauffeur, Course course,
                                     BigDecimal poidsProximite, BigDecimal poidsEquite,
                                     long coursesRecentesMax) {
        BigDecimal proximite = scoreProximite(chauffeur, course);
        BigDecimal equite = scoreEquite(chauffeur, coursesRecentesMax);
        return poidsProximite.multiply(proximite).add(poidsEquite.multiply(equite))
                .setScale(SCALE_SCORE, RoundingMode.HALF_UP);
    }

    /**
     * Critère de proximité normalisé sur [0, 1] : 1 si le chauffeur a terminé sa dernière course
     * à l'adresse de départ (ou si l'on n'a aucune information), décroissant jusqu'à 0
     * à {@link #DISTANCE_MAX_PROXIMITE_KM} km et au-delà.
     */
    private BigDecimal scoreProximite(Chauffeur chauffeur, Course course) {
        Optional<String> derniereDestination = courseRepository
                .findCoursesActivesByChauffeur(chauffeur.getId()).stream()
                .filter(c -> c.getStatut() == CourseStatut.ASSIGNEE)
                .map(Course::getDestination)
                .findFirst()
                .map(Optional::of)
                .orElse(derniereDestinationTerminee(chauffeur.getId()));

        if (derniereDestination.isEmpty()) {
            return BigDecimal.ONE;
        }

        double distanceKm = DistanceCalculator.estimerDistanceKm(derniereDestination.get(), course.getPointDepart());
        double score = Math.max(0.0, 1.0 - (distanceKm / DISTANCE_MAX_PROXIMITE_KM));
        return BigDecimal.valueOf(score).setScale(SCALE_SCORE, RoundingMode.HALF_UP);
    }

    private Optional<String> derniereDestinationTerminee(Long chauffeurId) {
        List<Course> terminees = courseRepository
                .findByChauffeurIdAndStatutOrderByDateHeureDesc(chauffeurId, CourseStatut.TERMINEE);
        return terminees.isEmpty() ? Optional.empty() : Optional.ofNullable(terminees.get(0).getDestination());
    }

    /**
     * Critère d'équité normalisé sur [0, 1] : 1 pour le chauffeur le moins sollicité
     * de la fenêtre glissante, 0 pour le plus sollicité.
     */
    private BigDecimal scoreEquite(Chauffeur chauffeur, long coursesRecentesMax) {
        if (coursesRecentesMax <= 0) {
            return BigDecimal.ONE;
        }
        LocalDateTime debutFenetre = LocalDateTime.now().minusDays(FENETRE_EQUITE_JOURS);
        long coursesRecentes = courseRepository.countByChauffeurIdAndDateHeureAfter(chauffeur.getId(), debutFenetre);
        double score = 1.0 - ((double) coursesRecentes / (double) coursesRecentesMax);
        return BigDecimal.valueOf(score).setScale(SCALE_SCORE, RoundingMode.HALF_UP);
    }

    /** Affecte chauffeur + véhicule à la course, en libérant l'ancien véhicule du chauffeur si besoin. */
    private void affecter(Course course, Candidat candidat) {
        Chauffeur chauffeur = candidat.chauffeur();
        Vehicule vehicule = candidat.vehicule();

        // Libère le véhicule s'il est encore rattaché à un autre chauffeur
        vehiculeRepository.findByChauffeurActuelId(chauffeur.getId())
                .filter(v -> !v.getId().equals(vehicule.getId()))
                .ifPresent(ancien -> {
                    ancien.setChauffeurActuel(null);
                    vehiculeRepository.save(ancien);
                });

        course.setChauffeur(chauffeur);
        course.setVehicule(vehicule);
        course.setStatut(CourseStatut.ASSIGNEE);
    }

    private Course getCourseEntity(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
    }

    private BigDecimal poidsProximite(ParametreTarif parametre) {
        if (parametre != null && parametre.getPoidsProximite() != null) {
            return parametre.getPoidsProximite();
        }
        return ParametreTarif.DEFAULT_POIDS_PROXIMITE;
    }

    private BigDecimal poidsEquite(ParametreTarif parametre) {
        if (parametre != null && parametre.getPoidsEquite() != null) {
            return parametre.getPoidsEquite();
        }
        return ParametreTarif.DEFAULT_POIDS_EQUITE;
    }

    private BigDecimal[] calculerTarifs(Double distanceKm) {
        var parametre = parametreTarifRepository.findAll().stream()
                .reduce((first, second) -> second)
                .orElseThrow(() -> new BusinessException(
                        "Aucun paramètre de tarif configuré. Veuillez en créer un avant de créer une course."));

        BigDecimal distance = BigDecimal.valueOf(distanceKm);
        BigDecimal tarif = parametre.getTarifBase().add(
                parametre.getPrixKm().multiply(distance));

        BigDecimal commission = tarif.multiply(parametre.getCommissionPourcentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal montantChauffeur = tarif.subtract(commission);

        return new BigDecimal[]{tarif.setScale(2, RoundingMode.HALF_UP), commission, montantChauffeur};
    }

    /**
     * Candidat à l'affectation : couple (chauffeur, véhicule) et score de pertinence associé
     * ({@code null} pour une affectation manuelle, où aucun score n'est calculé).
     */
    private record Candidat(Chauffeur chauffeur, Vehicule vehicule, BigDecimal score) {
    }
}
