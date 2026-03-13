package com.workforce.wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                            .requestMatchers("/api/ping").permitAll()
                            .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)
                            .requestMatchers("/api/employee/**").hasRole(ROLE_EMPLOYEE)
                            .requestMatchers("/api/work-entries/**").hasRole(ROLE_EMPLOYEE)
                            .anyRequest().authenticated()
                    )
                    .httpBasic(Customizer.withDefaults())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build SecurityFilterChain", e);
        }
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(WmsSecurityProperties props) {
        UserDetails admin = User.withUsername(props.admin().username())
                .password("{noop}" + props.admin().password())
                .roles(ROLE_ADMIN)
                .build();

        UserDetails employee = User.withUsername(props.employee().username())
                .password("{noop}" + props.employee().password())
                .roles(ROLE_EMPLOYEE)
                .build();

        return new InMemoryUserDetailsManager(admin, employee);
    }
}
