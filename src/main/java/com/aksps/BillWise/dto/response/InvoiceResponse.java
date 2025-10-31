// What it does: This class represents the response structure for an invoice, including customer details, line items, and financial totals.
// Why needed: It is used to transfer invoice data from the server to the client in the BillWise application.
package com.aksps.BillWise.dto.response;

import com.aksps.BillWise.dto.request.InvoiceItemRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private LocalDateTime invoiceDate;
    private String customerName;
    private String customerContactNumber;

    // line items structure for the response
    private List<InvoiceItemResponse> items;

    // Financial Totals
    private Double subTotal;
    private Double totalDiscount;
    private Double totalTax;
    private Double grandTotal;
}
