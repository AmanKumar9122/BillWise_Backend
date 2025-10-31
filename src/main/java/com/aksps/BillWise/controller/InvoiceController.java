package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.request.InvoiceRequest;
import com.aksps.BillWise.dto.response.InvoiceResponse;
import com.aksps.BillWise.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Invoice/Sales Transactions.
 * Exposes the core endpoint for processing a sale and deducting inventory.
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Processes a new sale transaction (CREATE operation).
     * This is the core billing endpoint.
     * Accessible by ADMIN and MANAGER roles, or potentially a specific 'CASHIER' role.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createInvoice(@Valid @RequestBody InvoiceRequest invoiceRequest) {
        try {
            InvoiceResponse processedInvoice = invoiceService.createInvoice(invoiceRequest);
            return new ResponseEntity<>(processedInvoice, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Handles errors like "Product not found"
            return ResponseEntity.badRequest().body("Error in Request: " + e.getMessage());
        } catch (IllegalStateException e) {
            // Handles critical errors like "Insufficient stock" (Inventory Check)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Transaction Failed: " + e.getMessage());
        } catch (RuntimeException e) {
            // Handles unexpected errors like DB transaction failure or Role not found
            return ResponseEntity.internalServerError().body("System Error during Invoice Creation: " + e.getMessage());
        }
    }

    // Optional: Add GET endpoints for retrieving invoice history for auditing/printing

    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        try {
            InvoiceResponse invoice = invoiceService.getInvoiceResponseById(id);
            return ResponseEntity.ok(invoice);
        } catch (IllegalArgumentException e) {
            // Returns 404 if the invoice is not found
            return ResponseEntity.notFound().build();
        }
    }
}