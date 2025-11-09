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
    public CommandLineRunner initDatabase(RoleRepository roleRepository) {

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
                    logger.info("Initialized role: " + roleName.name());
                }
            }

            // Log the completion of role initialization
            logger.info("Database role initialization complete. Total roles: " + roleRepository.count());
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