package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.RegisterRequest;
import com.aksps.BillWise.model.Role;
import com.aksps.BillWise.model.User;
import com.aksps.BillWise.model.RoleName;
import com.aksps.BillWise.repository.RoleRepository;
import com.aksps.BillWise.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Service dedicated to business logic related to User operations,
 * primarily registration and role management, handled within a transaction.
 */
@Service
public class UserService {
    // Repositories and encoder injected via constructor

    // Why RoleRepository is needed here?
    // To fetch and manage Role entities associated with Users during registration.

    // Why constructor injection is used here?
    // It promotes immutability and makes the class easier to test.
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    // Why passwordEncoder is used here?
    // To securely hash user passwords before storing them in the database.

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Handles user registration, ensuring the entire process (fetching roles, saving user)
     * occurs within a single, managed transaction.
     * @param registerRequest The registration data transfer object.
     * @return The newly saved User entity.
     */
    @Transactional
    public User registerNewUser(RegisterRequest registerRequest) {
        // Validation checks
        // Ensure username and email are unique

        // Why these checks are important?
        // To prevent duplicate accounts and maintain data integrity.

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        // 1. Create new user's account
        User user = new User(registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()));

        // Extract roles from request
        // Why using Set here?
        // To avoid duplicate role assignments for the user.
        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        // 2. Resolve and attach roles (roles are now "managed" by this transaction)
        // If no roles specified, assign default ROLE_USER

        // Why is default role assignment important?
        // It ensures every user has at least basic access permissions.

        if (strRoles == null || strRoles.isEmpty()) {
            // Default role if none specified: ROLE_USER
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Default role ROLE_USER not found in DB."));
            roles.add(userRole);
        } else {
            // Assign specified roles
            // Why validating roles here?
            // To ensure only recognized roles are assigned, preventing potential security issues.
            strRoles.forEach(role -> {
                RoleName roleName;
                try {
                    // Normalize role string to match enum naming
                    roleName = RoleName.valueOf(role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Handle invalid role string
                    throw new RuntimeException("Error: Invalid role specified: " + role);
                }

                // Fetch role from DB
                Role fetchedRole = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + roleName.name() + " not found."));
                roles.add(fetchedRole);
            });
        }

        // Attach resolved roles to user
        user.setRoles(roles);

        // 3. Save the user (Hibernate is happy because roles are managed in this transaction)
        return userRepository.save(user);
    }
}
