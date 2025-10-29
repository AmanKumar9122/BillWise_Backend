package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.request.LoginRequest;
import com.aksps.BillWise.dto.request.RegisterRequest;
import com.aksps.BillWise.dto.response.JwtResponse;
import com.aksps.BillWise.model.Role;
import com.aksps.BillWise.model.User;
import com.aksps.BillWise.model.RoleName;
import com.aksps.BillWise.repository.RoleRepository;
import com.aksps.BillWise.repository.UserRepository;
import com.aksps.BillWise.security.jwt.JwtTokenProvider;
import com.aksps.BillWise.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles public endpoints for user authentication: registration and login.
 * Delegates complex business logic (registration) to the UserService.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final UserRepository userRepository; // Inject UserRepository to fetch full details

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint for user login. Returns a JWT on success.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // 1. Get the authenticated principal (which is Spring's UserDetails implementation)
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        // 2. Look up the full User entity from the database using the username (FIX APPLIED HERE)
        User userDetails = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."));

        // 3. Extract roles from the userPrincipal's authorities
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // 4. Return the JWT response using the full JPA user details
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
    }

    /**
     * Endpoint for user registration. Delegates creation logic to the transactional UserService.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            userService.registerNewUser(registerRequest);
            return ResponseEntity.status(201).body("User registered successfully! Proceed to login.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }
}
