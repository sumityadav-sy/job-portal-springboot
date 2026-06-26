package com.sumit.jobportal.controller;

import com.sumit.jobportal.dto.LoginRequestDTO;
import com.sumit.jobportal.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication Management", description = "Endpoints for login using email and password")

public class AuthController {

        @Autowired
        private AuthenticationManager authenticationManager;
        // defined as a @Bean in SecurityConfig — Spring injects it here

        @Autowired
        private JwtUtil jwtUtil;

        @PostMapping("/login")
        @Operation(summary = "Login and get JWT token", description = "Authenticates the user with email and password. "
                        +
                        "Returns a signed JWT token to use as Bearer token in all subsequent requests. " +
                        "Token contains the user's email and role.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Login successful — JWT token returned"),
                        @ApiResponse(responseCode = "400", description = "Validation failed — email or password missing"),
                        @ApiResponse(responseCode = "401", description = "Invalid email or password")
        })

        // Login is public — no token needed to log in, that's the whole point
        @SecurityRequirements()
        public ResponseEntity<Map<String, String>> login(
                        @Valid @RequestBody LoginRequestDTO request) {

                // ── STEP 1: Build an unauthenticated token ─────────────────────
                // This is NOT a JWT — it's Spring Security's internal object
                // that wraps the credentials the user just submitted
                // Think of it as: "here are the credentials, please verify them"
                UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(
                                request.getEmail(), // principal (identifier)
                                request.getPassword() // credentials (raw password)
                );

                // ── STEP 2: Authenticate ───────────────────────────────────────
                // authenticationManager.authenticate() does all of this:
                // 1. calls UserDetailsServiceImpl.loadUserByUsername(email)
                // 2. calls BCryptPasswordEncoder.matches(rawPassword, storedHash)
                // 3. if match → returns a fully authenticated Authentication object
                // 4. if no match → throws BadCredentialsException → 401
                // We don't catch the exception here — GlobalExceptionHandler catches it
                Authentication authentication = authenticationManager.authenticate(credentials);

                // ── STEP 3: Extract role from the authenticated result ─────────
                // authentication.getAuthorities() returns the list we built in
                // UserDetailsServiceImpl — e.g. [ROLE_RECRUITER]
                // We take the first (and only) authority and strip the "ROLE_" prefix
                // so the JWT contains "RECRUITER" not "ROLE_RECRUITER"
                String role = authentication.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority) // "ROLE_RECRUITER"
                                .findFirst()
                                .orElse("UNKNOWN")
                                .replace("ROLE_", ""); // → "RECRUITER"

                // ── STEP 4: Generate JWT ───────────────────────────────────────
                // authentication.getName() returns the username = email
                // (set via .withUsername(email) in UserDetailsServiceImpl)
                String token = jwtUtil.generateToken(
                                authentication.getName(), // email
                                role // "RECRUITER" or "JOB_SEEKER"
                );

                // ── STEP 5: Return token ───────────────────────────────────────
                // Map.of() creates a simple JSON object: { "token": "eyJhbG..." }
                // The client stores this token and sends it with every future request
                return ResponseEntity.ok(Map.of("token", token));
        }
}