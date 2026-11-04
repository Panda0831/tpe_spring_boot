package com.tpe.taxis.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import com.tpe.taxis.dto.request.AssignCourseRequest;
import com.tpe.taxis.dto.request.CreateCourseRequest;
import com.tpe.taxis.dto.response.CourseResponse;
import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.Client;
import com.tpe.taxis.entity.Course;
import com.tpe.taxis.entity.CourseStatut;
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
import com.tpe.taxis.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final String COURSE_NOT_FOUND = "Course";

    private final CourseRepository courseRepository;
    private final ClientRepository clientRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ParametreTarifRepository parametreTarifRepository;

    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.clientId()));

        Chauffeur chauffeur = null;
        Vehicule vehicule = null;
        if (request.chauffeurId() != null) {
            chauffeur = getChauffeurEligible(request.chauffeurId());
            if (request.vehiculeId() != null) {
                vehicule = getVehiculeEligible(request.vehiculeId());
            } else {
                vehicule = chauffeur.getVehicule();
            }
            checkConflitChauffeur(chauffeur.getId());
        }

        BigDecimal[] montants = calculerTarifs(request.distanceKm());
        Course course = CourseMapper.toEntity(request, client, chauffeur, vehicule,
                montants[0], montants[1], montants[2], CourseStatut.EN_ATTENTE);

        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        return CourseMapper.toResponse(getCourseEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(CourseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(CourseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getCoursesByStatut(CourseStatut statut, Pageable pageable) {
        return courseRepository.findByStatut(statut, pageable)
                .map(CourseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getCoursesByClientId(Long clientId, Pageable pageable) {
        return courseRepository.findByClientId(clientId, pageable)
                .map(CourseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getCoursesByChauffeurId(Long chauffeurId, Pageable pageable) {
        return courseRepository.findByChauffeurId(chauffeurId, pageable)
                .map(CourseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getCoursesByVehiculeId(Long vehiculeId, Pageable pageable) {
        return courseRepository.findByVehiculeId(vehiculeId, pageable)
                .map(CourseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getCoursesByPeriode(LocalDateTime debut, LocalDateTime fin, Pageable pageable) {
        return courseRepository.findByDateHeureBetween(debut, fin, pageable)
                .map(CourseMapper::toResponse);
    }

    @Override
    public CourseResponse assignerCourse(Long courseId, AssignCourseRequest request) {
        Course course = getCourseEntity(courseId);

        if (course.getStatut() != CourseStatut.EN_ATTENTE) {
            throw new CourseConflictException(course.getId(),
                    "seule une course EN_ATTENTE peut être assignée (statut actuel : " + course.getStatut() + ")");
        }

        Chauffeur chauffeur = getChauffeurEligible(request.chauffeurId());
        checkConflitChauffeur(chauffeur.getId());

        Vehicule vehicule;
        if (request.vehiculeId() != null) {
            vehicule = getVehiculeEligible(request.vehiculeId());
        } else {
            vehicule = chauffeur.getVehicule();
        }
        if (vehicule == null) {
            throw new BusinessException(
                    "Le chauffeur (id: " + chauffeur.getId() + ") n'a aucun véhicule affecté.");
        }

        course.setChauffeur(chauffeur);
        course.setVehicule(vehicule);
        course.setStatut(CourseStatut.ASSIGNEE);

        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse demarrerCourse(Long courseId) {
        Course course = getCourseEntity(courseId);

        if (course.getStatut() != CourseStatut.ASSIGNEE) {
            throw new CourseConflictException(course.getId(),
                    "seule une course ASSIGNEE peut démarrer (statut actuel : " + course.getStatut() + ")");
        }

        course.setStatut(CourseStatut.EN_COURS);
        if (course.getChauffeur() != null) {
            course.getChauffeur().setStatut(com.tpe.taxis.entity.ChauffeurStatut.EN_COURSE);
        }
        if (course.getVehicule() != null) {
            course.getVehicule().setStatut(VehiculeStatut.EN_COURSE);
        }

        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse terminerCourse(Long courseId) {
        Course course = getCourseEntity(courseId);

        if (course.getStatut() != CourseStatut.EN_COURS) {
            throw new CourseConflictException(course.getId(),
                    "seule une course EN_COURS peut être terminée (statut actuel : " + course.getStatut() + ")");
        }

        course.setStatut(CourseStatut.TERMINEE);
        if (course.getChauffeur() != null) {
            course.getChauffeur().setStatut(com.tpe.taxis.entity.ChauffeurStatut.DISPONIBLE);
        }
        if (course.getVehicule() != null) {
            course.getVehicule().setStatut(VehiculeStatut.DISPONIBLE);
            course.getVehicule().setKilometrage(
                    course.getVehicule().getKilometrage() + (course.getDistanceKm() != null ? course.getDistanceKm() : 0.0));
        }

        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponse annulerCourse(Long courseId) {
        Course course = getCourseEntity(courseId);

        if (course.getStatut() == CourseStatut.TERMINEE || course.getStatut() == CourseStatut.ANNULEE) {
            throw new CourseConflictException(course.getId(),
                    "une course " + course.getStatut() + " ne peut pas être annulée");
        }

        course.setStatut(CourseStatut.ANNULEE);
        if (course.getChauffeur() != null
                && course.getChauffeur().getStatut() == com.tpe.taxis.entity.ChauffeurStatut.EN_COURSE) {
            course.getChauffeur().setStatut(com.tpe.taxis.entity.ChauffeurStatut.DISPONIBLE);
        }
        if (course.getVehicule() != null
                && course.getVehicule().getStatut() == VehiculeStatut.EN_COURSE) {
            course.getVehicule().setStatut(VehiculeStatut.DISPONIBLE);
        }

        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public void deleteCourse(Long id) {
        Course course = getCourseEntity(id);
        if (course.getStatut() == CourseStatut.EN_COURS) {
            throw new CourseConflictException(course.getId(),
                    "impossible de supprimer une course en cours");
        }
        courseRepository.delete(course);
    }

    /**
     * Calcule tarif, commission société et montant chauffeur à partir des paramètres de tarif.
     * Retourne {tarif, commission, montantChauffeur}.
     */
    private BigDecimal[] calculerTarifs(Double distanceKm) {
        var parametre = parametreTarifRepository.findAll().stream()
                .reduce((first, second) -> second) // dernier paramètre enregistré = actif
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

    private Course getCourseEntity(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(COURSE_NOT_FOUND, id));
    }

    private Chauffeur getChauffeurEligible(Long chauffeurId) {
        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
                .orElseThrow(() -> new ResourceNotFoundException("Chauffeur", chauffeurId));
        if (chauffeur.getStatut() == com.tpe.taxis.entity.ChauffeurStatut.SUSPENDU) {
            throw new com.tpe.taxis.exception.ChauffeurSuspenduException(
                    "Le chauffeur (id: " + chauffeurId + ") est suspendu.");
        }
        if (!chauffeur.isEligiblePourCourse()) {
            throw new com.tpe.taxis.exception.ChauffeurIndisponibleException(
                    chauffeur.getId(), chauffeur.getStatut());
        }
        return chauffeur;
    }

    private Vehicule getVehiculeEligible(Long vehiculeId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", vehiculeId));
        if (vehicule.getStatut() == VehiculeStatut.MAINTENANCE
                || vehicule.getStatut() == VehiculeStatut.HORS_SERVICE) {
            throw new com.tpe.taxis.exception.VehiculeEnMaintenanceException(
                    vehicule.getId(), vehicule.getImmatriculation(), vehicule.getStatut());
        }
        return vehicule;
    }

    private void checkConflitChauffeur(Long chauffeurId) {
        if (courseRepository.existsByChauffeurIdAndStatutIn(
                chauffeurId, List.of(CourseStatut.ASSIGNEE, CourseStatut.EN_COURS))) {
            throw new CourseConflictException("Le chauffeur (id: " + chauffeurId
                    + ") a déjà une course active en cours.");
        }
    }
}
