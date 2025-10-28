package com.aksps.BillWise.config;

import com.aksps.BillWise.model.Role;
import com.aksps.BillWise.model.RoleName;
import com.aksps.BillWise.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    // Logger for logging information during initialization
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean // bean to run code at application startup

    // This CommandLineRunner initializes the database with required roles if they do not exist
    public CommandLineRunner initDatabase(RoleRepository roleRepository) {

        // Lambda expression implementing CommandLineRunner
        return args -> {
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
