// What it does: This class represents a request to create an invoice, including customer contact number, total discount percentage, and a list of invoice items.
// Why needed: It is used to transfer invoice data from the client to the server when creating a new invoice in the BillWise application.

//package com.aksps.BillWise.dto.request;
//
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.*;
//import lombok.Data;
//
//import java.util.List;
//
//@Data
//public class InvoiceRequest {
//    @NotBlank(message = "Customer contact number is required")
//    @Size(min = 10, max = 10, message = "Contact number must be 10 digits")
//    private String customerContactNumber;
//
//    private Double totalDiscountPercentage;
//
//    @NotEmpty(message = "Invoice must contain at least one item")
//    @Valid
//    private List<InvoiceItemRequest> items;
//}

package com.aksps.BillWise.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * DTO representing a complete sales transaction request (The cart).
 * Includes customer details for lookup/creation and a list of items to be purchased.
 */
@Data
public class InvoiceRequest {

    // --- Customer Information ---
    // Contact number is used for lookups. Null/empty allows anonymous sales.
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
    private String customerContactNumber;

    // Customer name is optional but needed if the customer is new.
    private String customerName;

    // --- Line Items ---
    @Valid
    @NotEmpty(message = "Invoice must contain at least one item")
    private List<InvoiceItemRequest> items;

    // --- Financial Input ---
    // Can be used to apply a flat monetary discount to the final total.
    private Double totalDiscount;

    // NEW FIELD: Used to apply a flat percentage discount to the subtotal.
    private Double totalDiscountPercentage;
}


