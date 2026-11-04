package com.tpe.parking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Double accès exigé par le sujet :
 * <ul>
 *   <li><b>API REST ({@code /api/**})</b> : HTTP Basic, sans état, réponses 401/403 en JSON
 *   et matrice de rôles centralisée dans {@link SecurityPaths} ;</li>
 *   <li><b>Interface Web (Thymeleaf)</b> : form login avec session, pages d'erreur classiques.</li>
 * </ul>
 * Les deux chaînes partagent le même {@link CustomUserDetailsService} (comptes identifiés
 * par email) et le même {@link PasswordEncoder}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Règles « méthode + chemin → rôle minimum », de la plus restrictive à la moins
     * restrictive (la première règle qui matche gagne). Une action de niveau X est
     * autorisée pour X et tous les rôles supérieurs.
     */
    private static final List<Map.Entry<String, List<String>>> HIERARCHIE = List.of(
            Map.entry("ADMIN", List.of(SecurityPaths.ACTIONS_ADMIN)),
            Map.entry("MANAGER", List.of(SecurityPaths.ACTIONS_MANAGER)),
            Map.entry("OPERATOR", List.of(SecurityPaths.ACTIONS_OPERATOR)),
            Map.entry("USER", List.of(SecurityPaths.ACTIONS_USER))
    );

    // ------------------------------------------------------------------
    // Chaîne 1 : API REST (/api/**) — HTTP Basic, stateless
    // ------------------------------------------------------------------

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(this::configurerReglesApi)
                .httpBasic(org.springframework.security.config.Customizer.withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":401,\"message\":\"Authentification requise (HTTP Basic)\"}");
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":403,\"message\":\"Accès refusé : rôle insuffisant\"}");
                        }));
        return http.build();
    }

    /**
     * Matrice de rôles : chaque règle de {@link SecurityPaths} est convertie en
     * {@link RegexRequestMatcher} et restreinte au niveau indiqué et aux rôles supérieurs.
     * Toute autre requête API exige au minimum une authentification ; les contrôleurs ajoutent
     * des vérifications de propriétaire et des restrictions sur les données de gestion.
     */
    private void configurerReglesApi(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>
                    .AuthorizationManagerRequestMatcherRegistry auth) {
        for (Map.Entry<String, List<String>> niveau : HIERARCHIE) {
            String[] roles = rolesAutorises(niveau.getKey());
            for (String regle : niveau.getValue()) {
                String[] parties = regle.split(" ", 2);
                auth.requestMatchers(new RegexRequestMatcher(antVersRegex(parties[1]), parties[0]))
                        .hasAnyRole(roles);
            }
        }
        auth.anyRequest().authenticated();
    }

    /** Rôles habilités pour un niveau donné : le niveau et tous les rôles supérieurs. */
    private String[] rolesAutorises(String niveau) {
        return switch (niveau) {
            case "ADMIN" -> new String[]{"ADMIN"};
            case "MANAGER" -> new String[]{"MANAGER", "ADMIN"};
            case "OPERATOR" -> new String[]{"OPERATOR", "MANAGER", "ADMIN"};
            case "USER" -> new String[]{"USER", "OPERATOR", "MANAGER", "ADMIN"};
            default -> throw new IllegalArgumentException("Rôle inconnu : " + niveau);
        };
    }

    /**
     * Convertit un ant pattern en regex ancrée : {@code **} → zéro ou plusieurs segments,
     * {@code *} → un seul segment de chemin, littéraux quotés.
     *
     * <p>{@code /api/tarifs/**} couvre donc {@code /api/tarifs} <em>et</em> {@code /api/tarifs/1}
     * (même sémantique que {@code AntPathMatcher} pour un motif terminé par {@code /**}).</p>
     */
    private String antVersRegex(String ant) {
        StringBuilder regex = new StringBuilder("^");
        String[] segments = ant.split("/", -1);
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            boolean dernier = i == segments.length - 1;
            if ("**".equals(segment)) {
                // Termine le motif : « /... » optionnel pour couvrir le chemin parent.
                regex.append(dernier ? "(?:/.*)?$" : "/.*");
                return regex.toString();
            }
            if (segment.isEmpty()) {
                continue; // segment vide issu du slash initial
            }
            regex.append('/');
            regex.append("*".equals(segment) ? "[^/]+" : Pattern.quote(segment));
        }
        return regex.append('$').toString();
    }

    // ------------------------------------------------------------------
    // Chaîne 2 : interface Web (Thymeleaf) — form login avec session
    // ------------------------------------------------------------------

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
                // Protection CSRF active pour l'interface Web : Thymeleaf injecte
                // automatiquement le champ caché ``_csrf`` dans chaque formulaire ``th:action``.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/favicon.svg", "/error")
                        .permitAll()
                        .requestMatchers("/login")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", false)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }

    // ------------------------------------------------------------------
    // Commun
    // ------------------------------------------------------------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
