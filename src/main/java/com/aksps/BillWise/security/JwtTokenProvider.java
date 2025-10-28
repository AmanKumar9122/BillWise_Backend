package com.aksps.BillWise.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Utility class for generating, parsing, and validating JWT tokens.
 */
@Component
public class JwtTokenProvider {

    // Logger for logging JWT-related information
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // JWT secret key and expiration time are injected from application properties
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private int jwtExpirationInMs;

    /**
     * Generates a JWT token for the authenticated user.
     */
    public String generateToken(Authentication authentication) {

        // Get the user details from the authentication object
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        // Set token issue and expiration dates
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        // Build and return the JWT token
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())            // Set the subject as the username
                .setIssuedAt(now)                                   // Set the issue date
                .setExpiration(expiryDate)                          // Set the expiration date
                .signWith(SignatureAlgorithm.HS512, jwtSecret)      // Sign the token with the secret key
                .compact();                                         // Build the token
    }

    /**
     * Extracts the username from a JWT token.
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Validates a JWT token.
     */
    public boolean validateToken(String authToken) {
        // Try to parse the token to validate it
        try {
            // If parsing is successful, the token is valid
            Jwts.parser()                                       // Create a new JwtParser instance
                    .setSigningKey(jwtSecret)                   // Set the secret key used for signature validation
                    .build()                                    // Build the JwtParser
                    .parseClaimsJws(authToken);                 // Parse the JWT token
            return true;
        }
        // What is SignatureException ?
        // It is thrown when the JWT signature does not match locally computed signature
        catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        }
        // What is MalformedJwtException ?
        // It is thrown when the JWT was not correctly constructed
        catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }
        // What is ExpiredJwtException ?
        // It is thrown when the JWT has expired
        catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: {}", e.getMessage());
        }
        // What is UnsupportedJwtException ?
        // It is thrown when the JWT is of a type that is not supported
        catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        }
        // What is IllegalArgumentException ?
        // It is thrown when the claims string is empty or only whitespace
        catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
