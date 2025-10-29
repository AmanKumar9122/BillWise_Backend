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

    private Double baseUnit;

    private Integer currentStock;
    private Integer minStockLevel;

    public Product(String name, String sku, Double sellingPricePerBaseUnit, UnitType unitType, Double baseUnit, Integer currentStock, Integer minStockLevel) {
        this.name = name;
        this.sku = sku;
        this.sellingPricePerBaseUnit = sellingPricePerBaseUnit;
        this.unitType = unitType;
        this.baseUnit = baseUnit;
        this.currentStock = currentStock;
        this.minStockLevel = minStockLevel;
    }
}
