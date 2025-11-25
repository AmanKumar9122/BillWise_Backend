// What it does: DTO used to return invoice details back to clients including
//               customer summary, itemized billing details, and financial totals.
// Why needed: Cleanly separates response formatting from database entities.

package com.aksps.BillWise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    // Line item details for display in UI
    private List<InvoiceItemResponse> items;

    // Financial totals calculated during invoice creation
    private BigDecimal subTotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;
}
