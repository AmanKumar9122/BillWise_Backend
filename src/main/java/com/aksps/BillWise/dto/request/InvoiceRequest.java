package com.aksps.BillWise.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO for creating a new Invoice including customer metadata.
 */
public record InvoiceRequest(

        @NotBlank(message = "Customer name is required.")
        String customerName,

        @NotBlank(message = "Customer contact number is required.")
        @Size(min = 10, max = 10, message = "Contact number must be 10 digits.")
        String contactNumber,

        @Email(message = "Invalid email format.")
        String email,

        String gstNumber,

        @NotNull(message = "Tax rate is required.")
        Double taxRate,

        @Valid
        @NotEmpty(message = "Invoice must contain at least one item.")
        List<InvoiceItemRequest> items
) {}
