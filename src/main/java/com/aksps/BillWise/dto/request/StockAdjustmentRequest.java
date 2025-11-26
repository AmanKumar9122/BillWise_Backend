package com.aksps.BillWise.dto.request;

import com.aksps.BillWise.model.StockAdjustmentReason;
import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(

        @NotNull(message = "Product ID is required.")
        Long productId,

        @NotNull(message = "Quantity change is required.")
        Integer quantityChange,

        @NotNull(message = "Adjustment reason is required.")
        StockAdjustmentReason reason
) {}
