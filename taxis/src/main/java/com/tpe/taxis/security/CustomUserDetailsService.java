package com.tpe.taxis.security;

import com.tpe.taxis.entity.Chauffeur;
import com.tpe.taxis.entity.CompteUtilisateur;
import com.tpe.taxis.repository.CompteUtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Charge les comptes utilisateurs ({@link CompteUtilisateur}) pour Spring Security,
 * aussi bien pour le form login (Web) que pour le HTTP Basic (API REST).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CompteUtilisateurRepository compteUtilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CompteUtilisateur compte = compteUtilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun compte utilisateur pour le username : " + username));

        return new ComptePrincipal(compte);
    }

    /**
     * Principal applicatif : expose le compte JPA (id, rôle, chauffeur lié) en plus
     * du contrat UserDetails standard.
     */
    public record ComptePrincipal(CompteUtilisateur compte) implements UserDetails {

        @Override
        public List<GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_" + compte.getRole().name()));
        }

        @Override
        public String getPassword() {
            return compte.getMotDePasse();
        }

        @Override
        public String getUsername() {
            return compte.getUsername();
        }

        public Long getCompteId() {
            return compte.getId();
        }

        public String getRole() {
            return compte.getRole().name();
        }

        /** Chauffeur lié au compte, ou {@code null} pour les comptes administratifs. */
        public Chauffeur getChauffeur() {
            return compte.getChauffeur();
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
