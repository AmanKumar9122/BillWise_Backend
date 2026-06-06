package com.aksps.BillWise.dto.response;

import java.math.BigDecimal;

/**
 * DTO representing a top-performing product, including the aggregated metric.
 */
public record TopProductResponse(
        Long id,
        String name,
        String sku,
        String unitType,
        Integer currentStock,
        Long unitsSold,
        // The metric used for ranking (e.g., total revenue or total units sold)
        BigDecimal performanceMetric
) {}
