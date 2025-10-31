// What it does: DTO representing a single line item's details in the final Invoice response.
// Why needed: Separated for cleaner code organization.
package com.aksps.BillWise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single line item's details in the final Invoice response.
 * Separated for cleaner code organization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemResponse {
    private String productName;
    private String productSku;
    private Integer quantitySold;
    private Double unitPriceAtSale;
    private Double lineTotal;
}
