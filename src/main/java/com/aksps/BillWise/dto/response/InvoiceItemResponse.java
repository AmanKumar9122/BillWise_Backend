package com.aksps.BillWise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemResponse {
    private String productName;
    private String productSku;
    private Integer quantitySold;
    private BigDecimal unitPriceAtSale;
    private BigDecimal lineTotal;
}
