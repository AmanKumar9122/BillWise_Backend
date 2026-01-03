package com.aksps.BillWise.config;

import com.aksps.BillWise.model.Role;
import com.aksps.BillWise.model.RoleName;
import com.aksps.BillWise.repository.RoleRepository;
import org.slf4j.Logger; // Simple Logging Facade for Java
import org.slf4j.LoggerFactory; // Factory for creating Logger instances
import org.springframework.boot.CommandLineRunner; // Interface used to run code at application startup
import org.springframework.context.annotation.Bean; // Annotation to declare a bean
import org.springframework.context.annotation.Configuration; // Indicates that the class contains bean definitions

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    // Logger for logging information during initialization
    // LoggerFactory is used to create a logger instance for this class

    // why using static final?
    // static final ensures that there is only one instance of the logger for this class
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean // bean to run code at application startup
    // This CommandLineRunner initializes the database with required roles if they do not exist
    // and optionally creates an initial ADMIN user when ADMIN_* env vars are present.
    public CommandLineRunner initDatabase(RoleRepository roleRepository, com.aksps.BillWise.repository.UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {

        // Lambda expression implementing CommandLineRunner
        return args -> {
            // List of required roles to be initialized
            List<RoleName> requiredRoles = Arrays.asList(
                    RoleName.ROLE_USER,
                    RoleName.ROLE_MANAGER,
                    RoleName.ROLE_ADMIN
            );

            // Check and create roles if they do not exist
            for (RoleName roleName : requiredRoles) {
                // Check if the role already exists in the repository
                if (roleRepository.findByName(roleName).isEmpty()) {
                    // Save new role to the repository
                    roleRepository.save(new Role(null, roleName));
                    // Log the creation of the new role
                    logger.info("Initialized role: {}", roleName.name());
                }
            }

            // Log the completion of role initialization
            logger.info("Database role initialization complete. Total roles: {}", roleRepository.count());

            // Optional admin bootstrap
            String adminUsername = System.getenv("ADMIN_USERNAME");
            String adminEmail = System.getenv("ADMIN_EMAIL");
            String adminPassword = System.getenv("ADMIN_PASSWORD");

            boolean adminExists = userRepository.findAll().stream()
                    .flatMap(u -> u.getRoles().stream())
                    .anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN);

            if (!adminExists) {
                if (adminUsername != null && adminEmail != null && adminPassword != null) {
                    if (userRepository.existsByUsername(adminUsername) || userRepository.existsByEmail(adminEmail)) {
                        logger.warn("Admin username/email already exists. Skipping bootstrap admin creation.");
                    } else {
                        com.aksps.BillWise.model.User admin = new com.aksps.BillWise.model.User(adminUsername, adminEmail, passwordEncoder.encode(adminPassword));
                        com.aksps.BillWise.model.Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("ADMIN role not found while creating admin user."));
                        admin.getRoles().add(adminRole);
                        userRepository.save(admin);
                        logger.info("Created initial ADMIN user: {} (please change password on first login)", adminUsername);
                    }
                } else {
                    logger.info("No ADMIN bootstrap env vars provided. To create an initial admin user automatically set ADMIN_USERNAME, ADMIN_EMAIL, and ADMIN_PASSWORD environment variables before startup.");
                }
            } else {
                logger.info("ADMIN user already exists. Skipping bootstrap creation.");
            }
        };
    }
}

// Summary of DataInitializer.java
// Step	            Action	                           Description
//  1	           App starts	            Spring Boot loads all configuration classes
//  2	           Bean created	            CommandLineRunner bean is registered
//  3	           Roles listed	            Defines ROLE_USER, ROLE_MANAGER, ROLE_ADMIN
//  4	           Check database	        Looks up each role in RoleRepository
//  5	           Create missing roles	    Saves new roles if they don’t exist
//  6	           Logs results	            Prints logs confirming initialization