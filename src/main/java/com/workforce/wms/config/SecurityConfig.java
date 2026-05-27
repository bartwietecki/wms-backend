package com.workforce.wms.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            ObjectProvider<JwtDecoder> jwtDecoderProvider) {
        try {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .cors(Customizer.withDefaults())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                            .requestMatchers("/api/ping").permitAll()
                            .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)
                            .requestMatchers("/api/employee/**").hasRole(ROLE_EMPLOYEE)
                            .requestMatchers("/api/work-entries/**").hasRole(ROLE_EMPLOYEE)
                            .anyRequest().authenticated()
                    )
                    .httpBasic(Customizer.withDefaults());

            if (jwtDecoderProvider.getIfAvailable() != null) {
                configureJwtResourceServer(http);
            }

            return http.build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build SecurityFilterChain", e);
        }
    }

    private void configureJwtResourceServer(HttpSecurity http) {
        try {
            http.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure JWT resource server", e);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(WmsCorsProperties corsProps) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProps.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures JWT principal and role mapping for Keycloak tokens.
     * Uses preferred_username as principal so auth.getName() returns the same value
     * as it does for Basic Auth — keeping CurrentUserService.getCurrentEmployee() compatible
     * with both authentication paths without any code changes
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("preferred_username");
        converter.setJwtGrantedAuthoritiesConverter(this::extractKeycloakRoles);
        return converter;
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(WmsSecurityProperties props) {
        List<UserDetails> users = new ArrayList<>();

        users.add(User.withUsername(props.admin().username())
                .password("{noop}" + props.admin().password())
                .roles(ROLE_ADMIN)
                .build());

        for (WmsSecurityProperties.User emp : props.employees()) {
            users.add(User.withUsername(emp.username())
                    .password("{noop}" + emp.password())
                    .roles(ROLE_EMPLOYEE)
                    .build());
        }

        return new InMemoryUserDetailsManager(users);
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) {
            return List.of();
        }
        List<String> roles = (List<String>) realmAccess.getOrDefault("roles", List.of());
        return roles.stream()
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
