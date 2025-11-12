package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.YearMonth;

/**
 * JPA Entity to store aggregated historical sales data, structured for time-series analysis.
 * This is the input source for the external Machine Learning (ML) model.
 */
@Entity
@Table(name = "sales_data", uniqueConstraints = {
        // CRITICAL: Ensures only one volume entry per product per month
        @UniqueConstraint(columnNames = {"product_id", "sale_month"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FIX: Link to the product being tracked
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // FIX: Use YearMonth for monthly aggregation time dimension
    @Column(name = "sale_month", nullable = false)
    private YearMonth month;

    // FIX: Track physical volume (units), not monetary sales
    @Column(nullable = false)
    private Integer totalUnitsSold;

}