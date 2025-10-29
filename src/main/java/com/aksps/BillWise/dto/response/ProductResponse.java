package com.aksps.BillWise.dto.response;

import com.aksps.BillWise.model.UnitType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for responding with Product details to the client.
 * Provides necessary display information while hiding sensitive internal fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;

    // Price details for client display
    private Double sellingPricePerBaseUnit;
    private UnitType unitType;
    private String baseUnit;

    // Inventory status
    private Integer currentStock;
}

