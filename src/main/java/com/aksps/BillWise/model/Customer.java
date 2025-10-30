package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Customer {
    @Id
    @GeneratedValue (strategy =GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String contactNumber;
    private String email;

    // optional field
    private String gstNumber;
}
