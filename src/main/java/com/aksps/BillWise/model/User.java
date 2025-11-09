package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Stored as hashed value (BCrypt)

    // Eager fetching to load roles along with user
    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    // Why here we are using many to many ?
    // because one user can have multiple roles and one role can be assigned to multiple users

    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )

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
