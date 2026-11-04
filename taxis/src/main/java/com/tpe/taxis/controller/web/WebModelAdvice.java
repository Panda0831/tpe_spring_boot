package com.tpe.taxis.controller.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ajoute l'identité et le rôle de l'utilisateur connecté au modèle de toutes les vues web,
 * afin de construire la barre de navigation selon les droits, et transforme un refus
 * d'autorisation ({@code AccessDeniedException}) en page HTML 403 compréhensible.
 */
@ControllerAdvice(basePackages = "com.tpe.taxis.controller.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebModelAdvice {

    /** Refus d'accès sur une action Web (rôle insuffisant) : page d'erreur lisible en 403. */
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView accesRefuse(AccessDeniedException ex, HttpServletRequest request) {
        ModelAndView vue = new ModelAndView("error");
        vue.setStatus(HttpStatus.FORBIDDEN);
        vue.addObject("status", HttpStatus.FORBIDDEN.value());
        vue.addObject("error", "Accès refusé");
        vue.addObject("message", "Votre rôle ne permet pas cette action.");
        vue.addObject("path", request.getRequestURI());
        return vue;
    }

    @ModelAttribute("utilisateurConnecte")
    public String utilisateurConnecte(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }

    @ModelAttribute("roleConnecte")
    public String roleConnecte(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return null;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Indicateurs de droits dérivés de la hiérarchie ADMIN &gt; MANAGER &gt; OPERATOR &gt; USER,
     * exposés aux vues pour n'afficher que les actions autorisées (le contrôle réel reste
     * assuré par Spring Security en amont des contrôleurs).
     */
    @ModelAttribute("estAdmin")
    public boolean estAdmin(Authentication authentication) {
        return porte(authentication, "ADMIN");
    }

    @ModelAttribute("peutGerer")
    public boolean peutGerer(Authentication authentication) {
        return porte(authentication, "ADMIN", "MANAGER");
    }

    @ModelAttribute("peutOperer")
    public boolean peutOperer(Authentication authentication) {
        return porte(authentication, "ADMIN", "MANAGER", "OPERATOR");
    }

    private boolean porte(Authentication authentication, String... roles) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        Set<String> possedes = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .collect(Collectors.toSet());
        for (String role : roles) {
            if (possedes.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
