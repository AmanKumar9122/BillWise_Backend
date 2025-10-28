package com.aksps.BillWise.security.jwt;


import com.aksps.BillWise.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {
    // Logger for logging authentication-related information
    // JwtAuthFilter is a custom filter that intercepts HTTP requests to perform JWT authentication.
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    // Injecting JwtTokenProvider and CustomUserDetailsService
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // Overriding the doFilterInternal method to implement JWT authentication
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {
        try {
            // Extract JWT from the request
            String jwt = getJwtFromRequest(request);
            // Validate the token and set authentication in the security context
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Get username from the token
                String username = tokenProvider.getUsernameFromJWT(jwt);
                // Load user details from the database
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // Handle exceptions during authentication
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    // Helper method to extract JWT from the Authorization header
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Check if the Authorization header contains a Bearer token
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract and return the token
            return bearerToken.substring(7);
        }
        return null;
    }
}
