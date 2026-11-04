package com.tpe.taxis.controller.web;

import com.tpe.taxis.dto.request.CreateCourseRequest;
import com.tpe.taxis.dto.request.AssignCourseRequest;
import com.tpe.taxis.dto.response.CourseResponse;
import com.tpe.taxis.entity.CourseStatut;
import com.tpe.taxis.service.ChauffeurService;
import com.tpe.taxis.service.ClientService;
import com.tpe.taxis.service.CourseDispatcherService;
import com.tpe.taxis.service.CourseService;
import com.tpe.taxis.service.VehiculeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Gestion des courses : consultation paginée et filtrée, commande par un client,
 * attribution automatique (dispatch) ou manuelle, puis cycle de vie de la course
 * (démarrer, terminer, annuler). Toutes les actions passent par la même couche métier
 * que l'API REST.
 */
@Controller
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseWebController {

    private static final int TAILLE_PAGE = 10;

    private final CourseService courseService;
    private final CourseDispatcherService dispatcherService;
    private final ClientService clientService;
    private final ChauffeurService chauffeurService;
    private final VehiculeService vehiculeService;

    @GetMapping
    public String liste(@RequestParam(required = false) CourseStatut statut,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), TAILLE_PAGE,
                Sort.by(Sort.Direction.DESC, "dateHeure"));
        Page<CourseResponse> resultats = statut == null
                ? courseService.getAllCourses(pageable)
                : courseService.getCoursesByStatut(statut, pageable);

        model.addAttribute("page", resultats);
        model.addAttribute("statuts", CourseStatut.values());
        model.addAttribute("statutSelectionne", statut);
        model.addAttribute("clients", clientService.getAllClients());
        model.addAttribute("chauffeursEligibles", chauffeurService.getChauffeursEligiblesPourCourse());
        model.addAttribute("vehiculesDisponibles", vehiculeService.getVehiculesDisponiblesPourCourse());
        return "courses";
    }

    @PostMapping
    public String commander(@Valid @ModelAttribute CreateCourseRequest request,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/courses";
        }
        try {
            CourseResponse course = courseService.createCourse(request);
            redirectAttributes.addFlashAttribute("succes",
                    "Course #" + course.id() + " créée — tarif estimé " + course.tarif() + " Ar.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/courses";
    }

    /** Dispatch automatique : le moteur de score choisit le meilleur chauffeur éligible. */
    @PostMapping("/{id}/dispatcher")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String dispatcher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CourseResponse course = dispatcherService.dispatcherCourse(id);
            redirectAttributes.addFlashAttribute("succes",
                    "Course #" + id + " attribuée automatiquement à " + course.nomChauffeur()
                            + " (" + course.immatriculationVehicule() + ").");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/courses";
    }

    /** Affectation manuelle (chauffeur/véhicule imposés) avec les mêmes contrôles métier. */
    @PostMapping("/{id}/assigner")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String assigner(@PathVariable Long id,
                           @Valid @ModelAttribute AssignCourseRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("erreur", messageValidation(bindingResult));
            return "redirect:/courses";
        }
        try {
            CourseResponse course = request.chauffeurId() == null
                    ? dispatcherService.dispatcherCourse(id)
                    : dispatcherService.assignerManuellement(id, request.chauffeurId(), request.vehiculeId());
            redirectAttributes.addFlashAttribute("succes",
                    "Course #" + id + " attribuée à " + course.nomChauffeur()
                            + " (" + course.immatriculationVehicule() + ").");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/courses";
    }

    @PostMapping("/{id}/demarrer")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String demarrer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return executer(id, "démarrée", redirectAttributes, () -> courseService.demarrerCourse(id));
    }

    @PostMapping("/{id}/terminer")
    @PreAuthorize("hasAnyRole('OPERATOR','MANAGER','ADMIN')")
    public String terminer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return executer(id, "terminée", redirectAttributes, () -> courseService.terminerCourse(id));
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public String annuler(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return executer(id, "annulée", redirectAttributes, () -> courseService.annulerCourse(id));
    }

    /** Exécute une transition du cycle de vie en traduisant les erreurs métier en message flash. */
    private String executer(Long id, String libelle, RedirectAttributes redirectAttributes,
                            java.util.function.Supplier<CourseResponse> action) {
        try {
            action.get();
            redirectAttributes.addFlashAttribute("succes", "Course #" + id + " " + libelle + ".");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erreur", ex.getMessage());
        }
        return "redirect:/courses";
    }

    private String messageValidation(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Les informations de la course sont invalides.");
    }
}
