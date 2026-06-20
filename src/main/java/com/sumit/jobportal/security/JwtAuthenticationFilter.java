package com.sumit.jobportal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

        // ── STEP 1: Read the Authorization header ─────────────────────
        // Every authenticated request must carry:
        // Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        String authHeader = request.getHeader("Authorization");

        // ── STEP 2: Check header exists and has correct format ─────────
        // If no header or doesn't start with "Bearer " → skip everything
        // This handles public endpoints (/auth/login, GET /jobs, etc.)
        // They arrive with no Authorization header — that's perfectly fine
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // just continue the filter chain — no authentication set
            // Spring will check SecurityContext later and return 401
            // if the endpoint requires authentication
            filterChain.doFilter(request, response);
            return; // return here so we don't execute the rest of this method
        }

        // ── STEP 3: Extract token from header ─────────────────────────
        // "Bearer eyJhbG..." → "eyJhbG..."
        // substring(7) skips "Bearer " (7 characters including the space)
        String token = authHeader.substring(7);

        // ── STEP 4: Validate token ────────────────────────────────────
        // JwtUtil checks signature + expiry — pure math, no DB call
        // Invalid or expired → skip, do nothing, Spring returns 401
        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── STEP 5: Extract email from token ──────────────────────────
        // Token is valid — safe to read claims now
        String email = jwtUtil.extractEmail(token);

        // ── STEP 6: Check SecurityContext isn't already set ───────────
        // getAuthentication() returns null if nobody has authenticated
        // this request yet — which is the normal case here
        // This guard prevents processing the token twice if somehow
        // the filter runs more than once (shouldn't happen with
        // OncePerRequestFilter, but defensive programming)
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            // ── STEP 7: Load UserDetails from database ─────────────────
            // This is where UserDetailsServiceImpl.loadUserByUsername() is called
            // It fetches your User entity and wraps it in Spring's UserDetails
            // We need this to get the authorities (roles) for this user
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // ── STEP 8: Build the Authentication object ─────────────────
            // UsernamePasswordAuthenticationToken represents an authenticated user
            // Constructor: (principal, credentials, authorities)
            // principal   = the UserDetails object (who they are)
            // credentials = null (already verified via JWT — no password needed)
            // authorities = their roles e.g. [ROLE_RECRUITER]
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,        // principal
                            null,               // credentials — null because JWT verified
                            userDetails.getAuthorities() // [ROLE_RECRUITER] or [ROLE_JOB_SEEKER]
                    );

            // ── STEP 9: Attach request details to the auth token ────────
            // This adds metadata like IP address, session ID to the token
            // Useful for audit logs and Spring Security's internal tracking
            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // ── STEP 10: Put authentication into SecurityContext ─────────
            // THIS is the critical step — this is what tells Spring Security
            // "this request is authenticated, here is who they are"
            // After this line, Spring's authorization rules can check
            // getAuthentication().getAuthorities() to find their role
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // ── STEP 11: Continue the filter chain ────────────────────────
        // Whether we authenticated or not, always continue
        // If we set authentication → controller runs normally
        // If we didn't → Spring's authorization returns 401
        filterChain.doFilter(request, response);
    }
}