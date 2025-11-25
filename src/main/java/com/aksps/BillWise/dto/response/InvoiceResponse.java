package com.aksps.BillWise.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for responding with full Invoice details:
 * header, totals, and line items.
 */
public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        LocalDateTime invoiceDate,
        String customerName,
        BigDecimal subTotal,
        BigDecimal totalDiscount,
        BigDecimal totalTax,
        BigDecimal grandTotal,
        List<InvoiceItemResponse> items
) {}
