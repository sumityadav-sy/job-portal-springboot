package com.sumit.jobportal.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

// tells Spring: this class contains bean definitions
@Configuration

// tells Spring to scan for @PreAuthorize annotations and enforce them. Without
// it, @PreAuthorize is silently ignored — no error, no protection.
@EnableMethodSecurity
public class SecurityConfig {

        // JwtAuthenticationFilter will be built in Part 5
        // @Lazy = "don't try to create this bean yet during startup"
        // needed to avoid a circular dependency we'll explain when we get there
        @Lazy
        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        // @Bean = "Spring, manage this object. Anyone who @Autowires
        // PasswordEncoder gets this BCryptPasswordEncoder instance."
        //
        // Why PasswordEncoder (interface) and not BCryptPasswordEncoder (class)?
        // Same reason we use List<> not ArrayList<> — code to the interface.
        // Tomorrow if we switch to Argon2, nothing else changes.
        // ── BEAN 1: Password Encoder ──────────────────────────────────────────
        // Same as before — BCrypt with default cost factor 10
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // ── BEAN 2: Authentication Manager ───────────────────────────────────
        // Spring's AuthenticationManager handles the actual credential verification
        // during login — it delegates to UserDetailsService + PasswordEncoder
        // We need this as a bean so AuthController can @Autowire it
        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        // ── BEAN 3: The Security Filter Chain ────────────────────────────────
        // This is the main configuration — every HTTP request passes through this
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // ── CSRF: disabled ───────────────────────────────────────────
                                // REST API + JWT = no need for CSRF protection (explained above)
                                .csrf(csrf -> csrf.disable())

                                // ── SESSION POLICY: STATELESS ─────────────────────────────────
                                // Spring will NEVER create or use an HttpSession
                                // No session = no memory of users between requests
                                // Every request must carry a JWT to identify itself
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Inside securityFilterChain(), add after sessionManagement():
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        // This fires when request is UNAUTHENTICATED (no token /
                                                        // invalid token)
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        // This fires when request is AUTHENTICATED but wrong role
                                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to perform this action\"}");
                                                }))
                                // ── AUTHORIZATION RULES ───────────────────────────────────────
                                // Order matters — specific rules first, general rules last
                                .authorizeHttpRequests(auth -> auth

                                                // PUBLIC endpoints — no token required
                                                // Login + register must be open, otherwise nobody can ever get in
                                                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()

                                                // PUBLIC — Swagger (no HttpMethod needed, all methods on these paths
                                                // are safe)
                                                .requestMatchers(
                                                                "/swagger-ui.html", // Swagger UI entry point
                                                                "/swagger-ui/**", // Swagger UI static assets (JS,
                                                                                  // CSS,etc.)
                                                                "/v3/api-docs", // raw OpenAPI JSON spec
                                                                "/v3/api-docs/**" // OpenAPI spec sub-paths
                                                ).permitAll()

                                                // GET /jobs and GET /jobs/** — public job browsing, no login needed
                                                .requestMatchers(HttpMethod.GET, "/jobs").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/jobs/{id}").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/jobs/search").permitAll()

                                                // EVERYTHING ELSE requires authentication (valid JWT)
                                                .anyRequest().authenticated())

                                // ── JWT FILTER ────────────────────────────────────────────────
                                // Add our custom filter BEFORE Spring's default login filter
                                // Our filter runs first: reads JWT → validates → sets SecurityContext
                                // Then the authorization rules above can check the authenticated user
                                .addFilterBefore(jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}