// What this file does: This file defines the Invoice entity for the BillWise application,
//                      representing an invoice with its details, customer link, and associated line items.

package com.aksps.BillWise.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber; // Auto-generated/Sequence number

    private LocalDateTime invoiceDate = LocalDateTime.now();

    // Link to the Customer
    // what this does: Associates the invoice with a customer, allowing for tracking of customer-specific transactions.
    // manytoone relationship because many invoices can belong to one customer
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true) // Nullable because a transaction might be anonymous
    private Customer customer;

    // Line items associated with this invoice
    // what this does: Represents the individual items or services billed in this invoice.
    // onetomany relationship because one invoice can have many invoice items
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    // Financial Totals
    private Double subTotal;
    private Double totalDiscount;
    private Double totalTax;
    private Double grandTotal;
}
