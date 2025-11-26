package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.request.InvoiceRequest;
import com.aksps.BillWise.dto.response.InvoiceResponse;
import com.aksps.BillWise.exception.ResourceNotFoundException;
import com.aksps.BillWise.exception.ValidationException;
import com.aksps.BillWise.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.aksps.BillWise.dto.request.InvoiceFilterRequest;
import org.springframework.data.domain.Page;

/**
 * REST Controller for managing Invoice/Sales Transactions.
 * Exposes endpoints to create and fetch invoices.
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Core billing endpoint: creates a new invoice & deducts inventory.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createInvoice(@Valid @RequestBody InvoiceRequest invoiceRequest) {
        try {
            InvoiceResponse processed = invoiceService.createInvoice(invoiceRequest);
            return new ResponseEntity<>(processed, HttpStatus.CREATED);
        } catch (ResourceNotFoundException | ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body("System error during invoice creation: " + e.getMessage());
        }
    }

    /**
     * Fetch a single invoice by its ID (for viewing/printing/auditing).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getInvoiceById(@PathVariable Long id) {
        try {
            InvoiceResponse invoice = invoiceService.getInvoiceById(id);
            return ResponseEntity.ok(invoice);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Page<InvoiceResponse>> listInvoices(
            @ModelAttribute InvoiceFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<InvoiceResponse> invoices =
                invoiceService.getInvoices(filter, page, size, sortBy, direction);

        return ResponseEntity.ok(invoices);
    }

}
