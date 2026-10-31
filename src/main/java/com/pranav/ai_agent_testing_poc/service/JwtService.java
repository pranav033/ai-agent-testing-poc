
package com.pranav.ai_agent_testing_poc.service;

import com.pranav.ai_agent_testing_poc.security.AppUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {

        byte[] keyBytes = Decoders.BASE64.decode(secret);

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    // Generate JWT using immutable user ID as subject
    public String generateToken(AppUserDetails userDetails) {

        Date issuedAt = new Date();

        Date expiration = new Date(
                issuedAt.getTime() + expirationMs
        );

        return Jwts.builder()
                .subject(userDetails.getUserId().toString())
                .claim("username", userDetails.getUsername())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    // Extract username from a verified JWT
    public String extractUsername(String token) {

        Claims claims = parseClaims(token);

        return claims.get("username", String.class);
    }

    // Extract immutable user ID from JWT subject
    public String extractUserId(String token) {

        Claims claims = parseClaims(token);

        return claims.getSubject();
    }

    // Validate signature, user identity and expiration
    public boolean isTokenValid(
            String token,
            AppUserDetails userDetails) {

        try {

            Claims claims = parseClaims(token);

            String tokenUserId = claims.getSubject();

            String tokenUsername =
                    claims.get("username", String.class);

            Date expiration = claims.getExpiration();

            return tokenUserId != null
                    && tokenUserId.equals(
                            userDetails.getUserId().toString()
                    )
                    && tokenUsername != null
                    && tokenUsername.equals(userDetails.getUsername())
                    && expiration != null
                    && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException exception) {

            return false;
        }
    }

    // Verify JWT signature and extract claims
    private Claims parseClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
