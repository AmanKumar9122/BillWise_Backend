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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

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

        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        // 2. Resolve and attach roles (roles are now "managed" by this transaction)
        if (strRoles == null || strRoles.isEmpty()) {
            // Default role if none specified: ROLE_USER
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Default role ROLE_USER not found in DB."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                RoleName roleName;
                try {
                    roleName = RoleName.valueOf(role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Error: Invalid role specified: " + role);
                }

                Role fetchedRole = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + roleName.name() + " not found."));
                roles.add(fetchedRole);
            });
        }

        user.setRoles(roles);

        // 3. Save the user (Hibernate is happy because roles are managed in this transaction)
        return userRepository.save(user);
    }
}
