package com.sumit.jobportal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component  // Spring manages this — other classes can @Autowire it
public class JwtUtil {

    // @Value reads from application.properties
    // Spring injects the value at startup — no manual property loading needed
    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expiration}")
    private long expirationMs;  // 86400000 = 24 hours in milliseconds

    // ── PRIVATE HELPER: Build the signing Key object ──────────────────────
    // We can't use the raw String directly — JJWT needs a Key object
    // Keys.hmacShaKeyFor() converts our secret String bytes → a proper Key
    // Called internally by every method that touches the token
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes());
    }

    // ── METHOD 1: Generate Token ──────────────────────────────────────────
    public String generateToken(String email, String role) {
        return Jwts.builder()

            // subject = who this token identifies (we use email as the identifier)
            .setSubject(email)

            // custom claim — we embed role so we never need a DB call just for role
            .claim("role", role)

            // issued-at = current time (auto-converted to Unix timestamp)
            .setIssuedAt(new Date())

            // expiry = current time + 24 hours
            // after this point, validateToken() will return false automatically
            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))

            // sign with our secret key using HMAC-SHA256
            // this produces the third part of the JWT (the signature)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)

            // assemble Header.Payload.Signature and return as String
            .compact();
    }

    // ── METHOD 2: Validate Token ──────────────────────────────────────────
    public boolean validateToken(String token) {
        try {
            // parserBuilder() sets up the JWT parser
            // setSigningKey() tells it which key to verify the signature against
            // parseClaimsJws() does the actual verification:
            //   1. decodes header + payload
            //   2. recomputes signature using our key
            //   3. compares with incoming signature
            //   4. checks expiry claim
            // If ANY of these fail, it throws an exception
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);

            return true;  // no exception = token is valid + not expired

        } catch (Exception e) {
            // catches: expired token, invalid signature, malformed token, etc.
            // we don't need to distinguish — any failure = reject
            return false;
        }
    }

    // ── METHOD 3: Extract Email ───────────────────────────────────────────
    public String extractEmail(String token) {

        // getClaims() is a helper we build below — parses the token body
        // getSubject() reads the "sub" field we set in generateToken()
        return getClaims(token).getSubject();
    }

    // ── METHOD 4: Extract Role ────────────────────────────────────────────
    // Used in Part 5 (JwtAuthenticationFilter) to build the authority list
    public String extractRole(String token) {
        // get("role", String.class) reads our custom "role" claim
        return getClaims(token).get("role", String.class);
    }

    // ── PRIVATE HELPER: Parse and Return Claims ───────────────────────────
    // Both extractEmail and extractRole need to parse the token first
    // Extracting this avoids duplicating the parser setup in every method
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)  // parse + verify signature
                .getBody();             // .getBody() returns the Claims (payload)
    }
}