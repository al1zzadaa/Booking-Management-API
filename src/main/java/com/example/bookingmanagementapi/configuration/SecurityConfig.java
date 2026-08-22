package com.example.bookingmanagementapi.configuration;

import com.example.bookingmanagementapi.security.JwtFilterImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtFilterImpl jwtFilter;

    public SecurityConfig(JwtFilterImpl jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        // 1. PUBLIC ENDPOINTS (Authentication & Verification)
                        .requestMatchers(HttpMethod.POST,
                                "/auth/register",
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/forgot-password",
                                "/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/email-verification").permitAll()
                        .requestMatchers(HttpMethod.POST, "/email-verification/resend").permitAll()
// 2. PUBLIC READ-ONLY (Assuming anyone can view airlines and fare details)
                        .requestMatchers(HttpMethod.GET, "/airlines", "/airlines/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/fare-baggage", "/fare-baggage/**").permitAll()
// 3. AUTHENTICATED USER ENDPOINTS (Bookings, Favorites, Logout)
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/bookings",
                                "/bookings/**",
                                "/favorite-flights/**",
                                "/favorite-hotels/**").authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/bookings",
                                "/bookings/**",
                                "/favorite-flights",
                                "/favorite-hotels").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/bookings/**").authenticated()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/bookings/**",
                                "/favorite-flights/**",
                                "/favorite-hotels/**").authenticated()
// 4. ADMIN ENDPOINTS (System Management, Data Modification)
                        .requestMatchers(HttpMethod.POST, "/admins/makeAdmin").hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/airlines",
                                "/fare-baggage",
                                "/accounts",
                                "/emails/**").hasRole("ADMIN") // Restricts direct access to internal email triggers
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/airlines/**",
                                "/fare-baggage/**",
                                "/accounts/**").hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/accounts/block/**",
                                "/accounts/unblock/**").hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/airlines/**",
                                "/fare-baggage/**",
                                "/accounts/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/accounts",
                                "/accounts/**").hasRole("ADMIN")
// 5. CATCH-ALL (Good security practice to deny by default)
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/admins/**").hasRole("OWNER"))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(
                "ROLE_OWNER > ROLE_ADMIN \n ROLE_ADMIN > ROLE_USER"
        );
    }
}
