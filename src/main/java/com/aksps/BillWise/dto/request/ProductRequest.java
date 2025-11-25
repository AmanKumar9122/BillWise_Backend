package com.aksps.BillWise.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.aksps.BillWise.model.UnitType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required.")
    private String name;

    @NotBlank(message = "SKU is required.")
    private String sku;

    @NotNull(message = "Price per base unit is required.")
    @DecimalMin(value = "0.01", message = "Price per base unit must be greater than zero.")
    private BigDecimal sellingPricePerBaseUnit;

    @NotNull(message = "Unit type (WEIGHT, LIQUID, COUNT) is required.")
    private UnitType unitType;

    @NotBlank(message = "Base unit (e.g., g, ml, pc) is required.")
    private String baseUnit;

    @NotNull(message = "Current stock is required.")
    @Min(value = 0, message = "Current stock cannot be negative.")
    private Integer currentStock;

    @NotNull(message = "Minimum stock level for alerts is required.")
    @Min(value = 0, message = "Minimum stock level cannot be negative.")
    private Integer minStockLevel;
}
