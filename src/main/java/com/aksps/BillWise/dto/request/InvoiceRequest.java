package com.aksps.BillWise.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class InvoiceRequest {
    @NotBlank(message = "Customer contact number is required")
    @Size(min = 10, max = 10, message = "Contact number must be 10 digits")
    private String customerContactNumber;

    private Double totalDiscountPercentage;

    @NotEmpty(message = "Invoice must contain at least one item")
    @Valid
    private List<InvoiceItemRequest> items;
}
