package com.aksps.BillWise.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * JPA Entity representing a single line item within an Invoice.
 * It links a specific sale quantity to a specific product.
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

    // Link back to the parent invoice
    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // Link to the product sold (Note: Use Product entity, not ID)
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // The quantity sold (measured in the product's base unit)
    private Integer quantitySold;

    // The price per base unit *at the time of sale* (CRUCIAL for audit)
    private Double unitPriceAtSale;

    // The total price for this line item (quantity * unitPriceAtSale)
    private Double lineTotal;

    private Double itemDiscount;
}