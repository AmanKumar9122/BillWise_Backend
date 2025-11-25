package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.InvoiceItemRequest;
import com.aksps.BillWise.dto.request.InvoiceRequest;
import com.aksps.BillWise.dto.response.InvoiceItemResponse;
import com.aksps.BillWise.dto.response.InvoiceResponse;
import com.aksps.BillWise.exception.ResourceNotFoundException;
import com.aksps.BillWise.exception.ValidationException;
import com.aksps.BillWise.model.Invoice;
import com.aksps.BillWise.model.InvoiceItem;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.repository.InvoiceRepository;
import com.aksps.BillWise.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core service for processing sales invoices.
 * Handles inventory deduction, financial calculation, and transactional integrity.
 */
@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;

    // Centralized rounding rules
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int DECIMAL_SCALE = 2;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
    }

    /**
     * Creates a new Invoice, deducts stock, and calculates all financial totals.
     * This entire method is transactional to ensure atomicity.
     */
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {

        // 1. Build the invoice header
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(UUID.randomUUID().toString());  // Simple unique number
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setCustomerName(request.customerName());

        List<InvoiceItem> items = new ArrayList<>();
        BigDecimal runningSubtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO; // currently 0 — hook for future discounts

        // 2. Process each line item
        for (InvoiceItemRequest itemReq : request.items()) {

            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with ID: " + itemReq.productId()
                    ));

            // Stock check
            if (product.getCurrentStock() < itemReq.quantity()) {
                throw new ValidationException(
                        "Insufficient stock for product '" + product.getName() +
                                "'. Available: " + product.getCurrentStock() +
                                ", Requested: " + itemReq.quantity()
                );
            }

            // Deduct stock
            product.setCurrentStock(product.getCurrentStock() - itemReq.quantity());
            productRepository.save(product);

            // Financials for this line
            BigDecimal unitPrice = product.getSellingPricePerBaseUnit(); // BigDecimal in Product
            BigDecimal qty = BigDecimal.valueOf(itemReq.quantity());

            BigDecimal lineTotalBeforeDiscount = unitPrice
                    .multiply(qty)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE);

            // Placeholder for future per-item discounts
            BigDecimal itemDiscount = BigDecimal.ZERO;
            totalDiscount = totalDiscount.add(itemDiscount);

            BigDecimal lineTotal = lineTotalBeforeDiscount.subtract(itemDiscount);
            runningSubtotal = runningSubtotal.add(lineTotal);

            // Create InvoiceItem entity
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setQuantitySold(itemReq.quantity());
            item.setUnitPriceAtSale(unitPrice);
            item.setItemDiscount(itemDiscount);
            item.setLineTotal(lineTotal);

            items.add(item);
        }

        // 3. Tax & totals
        BigDecimal taxRate = BigDecimal.valueOf(request.taxRate()); // e.g. 0.18

        BigDecimal totalTax = runningSubtotal
                .multiply(taxRate)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE);

        BigDecimal grandTotal = runningSubtotal
                .add(totalTax)
                .setScale(DECIMAL_SCALE, ROUNDING_MODE);

        // 4. Fill invoice totals
        invoice.setSubTotal(runningSubtotal);
        invoice.setTotalDiscount(totalDiscount);
        invoice.setTotalTax(totalTax);
        invoice.setGrandTotal(grandTotal);
        invoice.setItems(items); // cascade via Invoice → InvoiceItem

        // 5. Persist
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // 6. Map to DTO
        return mapToInvoiceResponse(savedInvoice);
    }

    /**
     * Retrieve a single invoice by ID and map to DTO.
     */
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));

        return mapToInvoiceResponse(invoice);
    }

    // --- Mapping helpers ---

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        List<InvoiceItemResponse> itemResponses = invoice.getItems().stream()
                .map(this::mapToInvoiceItemResponse)
                .collect(Collectors.toList());

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getCustomerName(),
                invoice.getSubTotal(),
                invoice.getTotalDiscount(),
                invoice.getTotalTax(),
                invoice.getGrandTotal(),
                itemResponses
        );
    }

    private InvoiceItemResponse mapToInvoiceItemResponse(InvoiceItem item) {
        return new InvoiceItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantitySold(),
                item.getUnitPriceAtSale(),
                item.getLineTotal(),
                item.getItemDiscount()
        );
    }
}
