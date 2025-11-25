package com.aksps.BillWise.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for a single line item when creating a new Invoice.
 * Specifies which product and how many units are being purchased.
 */
public record InvoiceItemRequest(

        @NotNull(message = "Product ID is required for each invoice item.")
        Long productId,

        @NotNull(message = "Quantity sold is required.")
        @Min(value = 1, message = "Quantity sold must be at least 1.")
        Integer quantity
) {}
