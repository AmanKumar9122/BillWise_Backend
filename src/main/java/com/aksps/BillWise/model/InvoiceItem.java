package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Line item entity linking a product sold within an invoice.
 * Stores quantity and unit price at time of purchase for auditing accuracy.
 */
@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // link back to invoice
    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // link to the product sold
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer quantitySold;

    // price per unit at time of sale
    private BigDecimal unitPriceAtSale;

    // (quantitySold * unitPriceAtSale)
    private BigDecimal lineTotal;

    private BigDecimal itemDiscount = BigDecimal.ZERO;
}
