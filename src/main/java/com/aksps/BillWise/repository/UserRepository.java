package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Method to find a user by their username
    Optional<User> findByUsername(String username);

    // FIX: Add this method to resolve the error in AuthController.java
    Optional<User> findByEmail(String email);

    // Methods to check existence of username and email
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
