package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.report.InvoiceReportDetail;
import com.aksps.BillWise.dto.request.InvoiceFilterRequest;
import com.aksps.BillWise.dto.request.InvoiceRequest;
import com.aksps.BillWise.dto.response.InvoiceResponse;
import com.aksps.BillWise.exception.ResourceNotFoundException;
import com.aksps.BillWise.exception.ValidationException;
import com.aksps.BillWise.service.InvoiceService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Invoice / Sales Transactions.
 * Features:
 *  - Create new invoice (billing)
 *  - Fetch single invoice
 *  - List invoices with filtering + pagination
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Create new invoice (core billing action)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createInvoice(@Valid @RequestBody InvoiceRequest invoiceRequest) {
        try {
            InvoiceResponse processed = invoiceService.createInvoice(invoiceRequest);
            return new ResponseEntity<>(processed, HttpStatus.CREATED);

        } catch (ResourceNotFoundException | ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("System error during invoice creation: " + e.getMessage());
        }
    }

    /**
     * Get a single invoice (view/print/audit)
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

    /**
     * NEW:
     * Invoice listing (search + filter + pagination)
     *
     * Supports:
     *  - customerName search
     *  - date range (startDate, endDate)
     *  - page, size
     *  - sorting (invoiceDate, grandTotal, etc.)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Page<InvoiceResponse>> listInvoices(
            @ModelAttribute InvoiceFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Page<InvoiceResponse> invoices = invoiceService.getInvoices(
                filter, page, size, sortBy, direction
        );
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}/print")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<?> getPrintableInvoice(@PathVariable Long id) {
        try {
            InvoiceReportDetail report = invoiceService.getPrintableInvoice(id);
            return ResponseEntity.ok(report);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
