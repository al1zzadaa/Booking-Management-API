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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/auth/register",
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/forgot-password",
                                "/auth/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/accounts").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/accounts/{accountId}").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/accounts/{accountId}/currency").authenticated()
                        .requestMatchers(HttpMethod.GET, "/accounts/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/accounts", "/accounts/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/accounts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/accounts/block/{id}", "/accounts/unblock/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/admins/**").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/airlines", "/airlines/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/airlines").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/airlines/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/airlines/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/bookings").authenticated()
                        .requestMatchers(HttpMethod.POST, "/bookings/pay/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/bookings/cancel/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/bookings/my-history").authenticated()
                        .requestMatchers(HttpMethod.GET, "/bookings/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/bookings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/bookings/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/bookings/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/email-verification").permitAll()
                        .requestMatchers(HttpMethod.POST, "/email-verification/resend").permitAll()
                        .requestMatchers(HttpMethod.POST, "/fare-baggage").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/fare-baggage/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/fare-baggage/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/fare-baggage/*",
                                "/fare-baggage"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/favorite-flights").authenticated()
                        .requestMatchers(HttpMethod.GET, "/favorite-flights/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/favorite-flights/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/favorite-hotels").authenticated()
                        .requestMatchers(HttpMethod.GET, "/favorite-hotels/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/favorite-hotels/*").authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/flights",
                                "/flights/*"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/flights").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/flights/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/flights/*").hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/flight-reviews",
                                "/flight-reviews/*"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/flight-reviews").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/flight-reviews/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/flight-reviews/*").authenticated()







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
