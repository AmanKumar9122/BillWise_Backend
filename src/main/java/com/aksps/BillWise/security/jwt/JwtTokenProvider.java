package com.aksps.BillWise.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct; // New Import
import java.security.Key;
import java.util.Date;

/**
 * Utility class for generating, validating, and extracting user information from JWT tokens.
 * Uses @PostConstruct to safely initialize the secret key after @Value fields are populated.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Fields injected by Spring
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    // Fields initialized after Spring injection (in @PostConstruct)
    private Key key;
    private JwtParser jwtParser;

    // Constructor is simplified and no longer tries to initialize the key prematurely
    public JwtTokenProvider() {
        // Empty constructor, Spring handles property injection
    }

    /**
     * Executes immediately after the bean is constructed and all @Value fields are set.
     * This guarantees the 'jwtSecret' is available before we try to use it.
     */
    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT Secret key must be configured (app.jwtSecret) and cannot be empty.");
        }

        // 1. Initialize the Key (Fixes WeakKeyException)
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));

        // 2. Initialize the Parser (Fixes parserBuilder() compilation error)
        this.jwtParser = Jwts.parser()
                .setSigningKey(this.key)
                .build();
    }


    /**
     * Generates a JWT based on the authenticated user's details.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extracts the username from a JWT.
     */
    public String getUsernameFromJWT(String token) {
        // Use the pre-initialized parser field directly
        return jwtParser.parseClaimsJws(token)
                .getBody().getSubject();
    }

    /**
     * Validates the integrity and expiration of a JWT.
     */
    public boolean validateToken(String authToken) {
        try {
            // Use the pre-initialized parser field directly
            jwtParser.parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}