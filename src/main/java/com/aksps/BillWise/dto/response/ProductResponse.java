package com.aksps.BillWise.dto.response;

import com.aksps.BillWise.model.UnitType;

import java.math.BigDecimal;

/**
 * DTO for sending Product details to the client (read-only view).
 * Excludes sensitive data like cost price.
 */
public record ProductResponse(
        Long id,
        String name,
        String sku,
        BigDecimal sellingPricePerBaseUnit,
        UnitType unitType,
        String baseUnit,
        Integer currentStock
) {}