// Description: This is the Product entity class representing products in the BillWise application.
// Why this file is needed: It defines the structure of the Product entity, including its attributes and database mapping,
//                          which is essential for managing product information and inventory in the billing system.

package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String sku;

    private Double sellingPricePerBaseUnit;

    @Enumerated(EnumType.STRING)
    private UnitType unitType;

    private String baseUnit;

    private Integer currentStock;
    private Integer minStockLevel;

    public Product(String name, String sku, Double sellingPricePerBaseUnit, UnitType unitType, String baseUnit, Integer currentStock, Integer minStockLevel) {
        this.name = name;
        this.sku = sku;
        this.sellingPricePerBaseUnit = sellingPricePerBaseUnit;
        this.unitType = unitType;
        this.baseUnit = baseUnit;
        this.currentStock = currentStock;
        this.minStockLevel = minStockLevel;
    }
}
