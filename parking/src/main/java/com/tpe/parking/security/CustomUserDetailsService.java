package com.tpe.parking.security;

import com.tpe.parking.entity.Utilisateur;
import com.tpe.parking.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Charge les comptes ({@link Utilisateur}, identifiés par leur email) pour Spring Security,
 * aussi bien pour le form login (Web) que pour le HTTP Basic (API REST).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun compte pour l'identifiant : " + username));

        return new UtilisateurPrincipal(utilisateur);
    }

    /**
     * Principal applicatif : expose l'utilisateur JPA (id, rôle, email) en plus
     * du contrat UserDetails standard.
     */
    public record UtilisateurPrincipal(Utilisateur utilisateur) implements UserDetails {

        @Override
        public List<GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()));
        }

        @Override
        public String getPassword() {
            return utilisateur.getMotDePasse();
        }

        @Override
        public String getUsername() {
            return utilisateur.getEmail();
        }

        public Long getUtilisateurId() {
            return utilisateur.getId();
        }

        public String getRole() {
            return utilisateur.getRole().name();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
