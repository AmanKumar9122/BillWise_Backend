package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private String fieldName;      // e.g., "currentStock", "sellingPrice", "name"
    private String oldValue;
    private String newValue;

    private String changedBy;      // username from JWT

    private LocalDateTime timestamp = LocalDateTime.now();
}
