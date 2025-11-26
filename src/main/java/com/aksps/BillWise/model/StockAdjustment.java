package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustments")
@Data
@NoArgsConstructor
public class StockAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Integer quantityChange;

    @Enumerated(EnumType.STRING)
    private StockAdjustmentReason reason;

    private LocalDateTime adjustedAt = LocalDateTime.now();

    private String adjustedBy; // extracted from JWT
}
