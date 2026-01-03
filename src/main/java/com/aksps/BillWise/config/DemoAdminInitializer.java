package com.aksps.BillWise.config;

import com.aksps.BillWise.model.Role;
import com.aksps.BillWise.model.RoleName;
import com.aksps.BillWise.model.User;
import com.aksps.BillWise.repository.RoleRepository;
import com.aksps.BillWise.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DemoAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoAdminInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String adminEmail = "admin@billwise.com";
        String adminUsername = "admin";

        // 1️⃣ Check if admin already exists
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }


        // 2️⃣ Ensure ADMIN role exists
        Role adminRole = roleRepository
                .findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.ROLE_ADMIN);
                    return roleRepository.save(role);
                });

        // 3️⃣ Create admin user
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("Admin@123"));

        // 4️⃣ Assign role
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        admin.setRoles(roles);

        // 5️⃣ Save admin
        userRepository.save(admin);

        System.out.println(
                "✅ Demo Admin created → email: admin@billwise.com | password: Admin@123"
        );
    }
}
