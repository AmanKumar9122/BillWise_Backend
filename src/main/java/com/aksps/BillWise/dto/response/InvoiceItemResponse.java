package com.aksps.BillWise.dto.response;

import java.math.BigDecimal;

/**
 * DTO representing a single line item in an Invoice response.
 */
public record InvoiceItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantitySold,
        BigDecimal unitPriceAtSale,
        BigDecimal lineTotal,
        BigDecimal itemDiscount
) {}
