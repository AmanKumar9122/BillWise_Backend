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
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true) // Nullable because a transaction might be anonymous
    private Customer customer;

    // Line items associated with this invoice
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    // Financial Totals
    private Double subTotal;
    private Double totalDiscount;
    private Double totalTax;
    private Double grandTotal;
}
