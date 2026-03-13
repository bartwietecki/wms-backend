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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                            .requestMatchers("/api/ping").permitAll()
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")
                            .requestMatchers("/api/work-entries/**").hasRole("EMPLOYEE")
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
                .roles("ADMIN")
                .build();

        UserDetails employee = User.withUsername(props.employee().username())
                .password("{noop}" + props.employee().password())
                .roles("EMPLOYEE")
                .build();

        return new InMemoryUserDetailsManager(admin, employee);
    }
}
