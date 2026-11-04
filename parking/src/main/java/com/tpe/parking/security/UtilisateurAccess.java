package com.tpe.parking.security;

import com.tpe.parking.dto.response.ReservationResponse;
import com.tpe.parking.exception.ResourceNotFoundException;
import com.tpe.parking.security.CustomUserDetailsService.UtilisateurPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Objects;

public final class UtilisateurAccess {

    private UtilisateurAccess() {
    }

    public static boolean estUtilisateur(UtilisateurPrincipal principal) {
        return role(principal).equals("USER");
    }

    public static Long id(UtilisateurPrincipal principal) {
        return applicatif(principal).getUtilisateurId();
    }

    public static Long verifierFiltreUtilisateur(UtilisateurPrincipal principal, Long utilisateurId) {
        String role = role(principal);
        if (role.equals("USER")) {
            if (utilisateurId != null) {
                verifierProprietaire(principal, utilisateurId);
            }
            return id(principal);
        }
        return utilisateurId;
    }

    public static void verifierProprietaire(UtilisateurPrincipal principal, Long proprietaireId) {
        if (role(principal).equals("USER")
                && !Objects.equals(applicatif(principal).getUtilisateurId(), proprietaireId)) {
            throw new AccessDeniedException("Accès limité aux données de votre compte.");
        }
    }

    public static ReservationResponse verifierProprietaire(
            UtilisateurPrincipal principal, ReservationResponse reservation) {
        if (reservation == null) {
            throw new ResourceNotFoundException("Réservation introuvable.");
        }
        verifierProprietaire(principal, reservation.utilisateurId());
        return reservation;
    }

    public static List<ReservationResponse> filtrerReservationsUtilisateur(
            UtilisateurPrincipal principal, List<ReservationResponse> reservations) {
        if (!role(principal).equals("USER")) {
            return reservations;
        }
        Long utilisateurId = id(principal);
        return reservations.stream()
                .filter(reservation -> Objects.equals(utilisateurId, reservation.utilisateurId()))
                .toList();
    }

    private static String role(UtilisateurPrincipal principal) {
        if (principal == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getAuthorities() != null) {
                for (GrantedAuthority authority : authentication.getAuthorities()) {
                    String granted = authority.getAuthority();
                    if (granted.equals("ROLE_ADMIN") || granted.equals("ROLE_MANAGER")
                            || granted.equals("ROLE_OPERATOR")) {
                        return granted.substring("ROLE_".length());
                    }
                }
            }
            throw new AccessDeniedException("Authentification applicative requise.");
        }
        return applicatif(principal).getRole();
    }

    private static UtilisateurPrincipal applicatif(UtilisateurPrincipal principal) {
        if (principal == null || principal.utilisateur() == null || principal.getUtilisateurId() == null
                || principal.utilisateur().getRole() == null) {
            throw new AccessDeniedException("Authentification applicative requise.");
        }
        return principal;
    }
}
