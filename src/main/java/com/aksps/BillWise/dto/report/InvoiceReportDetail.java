package com.aksps.BillWise.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceReportDetail(
        Long invoiceId,
        String invoiceNumber,
        LocalDateTime invoiceDate,

        // Customer full details
        String customerName,
        String customerContact,
        String customerEmail,
        String customerGst,

        // Store/company details (optional, static for now)
        String storeName,
        String storeAddress,
        String storeContact,

        // Line items
        List<ReportItem> items,

        BigDecimal subTotal,
        BigDecimal totalDiscount,
        BigDecimal totalTax,
        BigDecimal grandTotal
) {

    public record ReportItem(
            String productName,
            String sku,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            BigDecimal discount
    ) {}
}
