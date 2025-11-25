package com.aksps.BillWise.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO for creating a new Invoice.
 * Contains customer display name, tax rate, and the list of invoice items.
 */
public record InvoiceRequest(

        @NotBlank(message = "Customer name is required.")
        String customerName,

        @NotNull(message = "Tax rate is required.")  // e.g. 0.18 for 18% GST
        Double taxRate,

        @Valid
        @NotEmpty(message = "Invoice must contain at least one item.")
        List<InvoiceItemRequest> items
) {}
