package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

// Entity annotation to specify that this class is a JPA entity
@Entity

// Defining the table name in the database
@Table(name = "users")

// Lombok annotations to generate boilerplate code like getters, setters, constructors
@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields as parameters

// User class representing a user in the system
public class User {
    @Id // Primary key of the entity.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the primary key value using the database's identity column feature.
    private Long id; // Unique identifier for each user

    @Column(nullable = false, unique = true) // username column must be unique and cannot be null
    private String username; // Username of the user

    @Column(nullable = false, unique = true) // email column must be unique and cannot be null
    private String email; // Email of the user

    @Column(nullable = false) // password column cannot be null
    private String password; // Stored as hashed value (BCrypt)

    // What is BCrypt ?
    // BCrypt is a password hashing function designed to be computationally intensive to protect against brute-force attacks.
    // It incorporates a salt to protect against rainbow table attacks and is adaptive, meaning the iteration count can be
    // increased over time to counteract advances in hardware capabilities.

    // Eager fetching to load roles along with user
    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    // Why here we are using many to many ?
    // because one user can have multiple roles and one role can be assigned to multiple users

    // Join table to manage the many-to-many relationship between users and roles
    @JoinTable(
        name = "user_roles", // Name of the join table
        joinColumns = @JoinColumn(name = "user_id"), // Foreign key column referencing the user
        inverseJoinColumns = @JoinColumn(name = "role_id") // Foreign key column referencing the role
    )

    // What is the use of inverseJoinColumns here ?
    // The inverseJoinColumns attribute specifies the foreign key column in the join table that references the
    // primary key of the associated entity (Role in this case). It defines how the many-to-many relationship is mapped
    // from the perspective of the Role entity.

    // why set ? - to avoid duplicate roles for a user
    private Set<Role> roles = new HashSet<>();

//     Constructor used for registering new users
//     Why not including id and roles in constructor ?
//     id is auto-generated and roles can be assigned later

    public User(String username ,String email , String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
