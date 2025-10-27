package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleName name;

    private enum RoleName{
        ROLE_USER,                  // Standard application user (eg- sales clerk)
        ROLE_MANAGER,               // Can perform analysis and forecasting (eg- store manager)
        ROLE_ADMIN                  // Full administrative privileges (eg- business owner)
    }
}
