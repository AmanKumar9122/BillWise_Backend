// Description: DTO for capturing invoice item details in BillWise application
// Why needed: This class is used to transfer data related to invoice items when creating or updating invoices.

package com.aksps.BillWise.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoiceItemRequest {
    // SKU of the product being sold
    // SKU is used instead of product ID to simplify client-side operations
    // SKU - Stock Keeping Unit, a unique identifier for each product
    @NotBlank(message = "Product SKU is required")
    private String productSku;

    // Quantity of the product sold in this invoice item
    // Must be at least 1
    @NotNull(message = "Quantity sold is required")
    @Min(value = 1, message = "Quantity sold must be at least 1")
    private Integer quantitySold;
}
