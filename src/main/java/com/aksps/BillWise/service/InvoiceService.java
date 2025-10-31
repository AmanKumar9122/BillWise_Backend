package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.InvoiceItemRequest;
import com.aksps.BillWise.dto.request.InvoiceRequest;
import com.aksps.BillWise.dto.request.CustomerRequest;
import com.aksps.BillWise.dto.response.InvoiceResponse;
import com.aksps.BillWise.dto.response.InvoiceItemResponse;
import com.aksps.BillWise.model.Customer;
import com.aksps.BillWise.model.Invoice;
import com.aksps.BillWise.model.InvoiceItem;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.repository.InvoiceRepository;
import com.aksps.BillWise.repository.ProductRepository;
import com.aksps.BillWise.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * The core service for processing sales transactions.
 * Handles financial calculations, customer management, and atomic inventory updates.
 */
@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    // Assuming a simple 18% GST/Tax rate for demonstration
    private static final double TAX_RATE = 0.18;

    public InvoiceService(InvoiceRepository invoiceRepository, ProductRepository productRepository,
                          CustomerService customerService, CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    /**
     * Processes a new sale transaction (The core of the billing system).
     *
     * @param request The complete invoice data from the client.
     * @return The detailed Invoice Response DTO.
     */
    @Transactional(rollbackFor = Exception.class) // CRITICAL: Ensures atomicity
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        // --- 1. Customer Management: Lookup or Create on Checkout ---
        Customer customer = handleCustomer(request.getCustomerContactNumber(), request.getCustomerName());

        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(LocalDateTime.now());

        // --- 2. Process Items, Check Stock, Deduct Stock, and Calculate Totals ---
        AtomicReference<Double> subTotalRef = new AtomicReference<>(0.0);
        List<InvoiceItem> savedItems = processAndSaveItems(invoice, request.getItems(), subTotalRef);

        invoice.setItems(savedItems);
        invoice.setSubTotal(subTotalRef.get());

        // --- 3. Final Financial Calculation ---
        // Calculate discount amount based on percentage
        double totalDiscountRate = request.getTotalDiscountPercentage() != null ? request.getTotalDiscountPercentage() : 0.0;
        double totalDiscountAmount = invoice.getSubTotal() * (totalDiscountRate / 100.0);

        invoice.setTotalDiscount(totalDiscountAmount);

        double taxableSubtotal = invoice.getSubTotal() - invoice.getTotalDiscount();
        double totalTax = taxableSubtotal * TAX_RATE;
        invoice.setTotalTax(totalTax);
        invoice.setGrandTotal(taxableSubtotal + totalTax);

        // --- 4. Final Persistence ---
        // Simple sequential number for invoice number
        String newInvoiceNumber = "INV-" + (invoiceRepository.count() + 1);
        invoice.setInvoiceNumber(newInvoiceNumber);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // --- 5. Map and Return Response ---
        return mapToResponse(savedInvoice);
    }

    /**
     * Helper to lookup customer by contact or create a new one if not found.
     * This implements the "auto-save new customer on checkout" feature.
     */
    private Customer handleCustomer(String contactNumber, String name) {
        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            return null; // Anonymous sale
        }

        // Use the Repository directly to fetch the entity for transactional linking
        Optional<Customer> existingCustomerEntity = customerRepository.findByContactNumber(contactNumber);

        if (existingCustomerEntity.isPresent()) {
            return existingCustomerEntity.get(); // Found existing customer entity
        } else {
            // New customer: Create and persist the new record immediately
            CustomerRequest newCustomerRequest = new CustomerRequest(
                    name != null ? name : "Anonymous", // Use provided name or default
                    contactNumber,
                    null, // Email is optional
                    null  // GST is optional
            );

            // This relies on CustomerService.createCustomer returning the saved Customer Entity
            // For safety, we rely on the repository to fetch the managed entity after creation.
            customerService.createCustomer(newCustomerRequest);

            // Fetch the newly created managed entity for proper transactional linking
            return customerRepository.findByContactNumber(contactNumber)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve newly created customer."));
        }
    }


    /**
     * Processes all items in the request: validates stock, deducts inventory, and creates InvoiceItem entities.
     */
    private List<InvoiceItem> processAndSaveItems(Invoice invoice, List<InvoiceItemRequest> itemRequests, AtomicReference<Double> subTotalRef) {
        List<InvoiceItem> items = new ArrayList<>();
        double runningSubtotal = 0.0;

        for (InvoiceItemRequest reqItem : itemRequests) {
            Product product = productRepository.findBySku(reqItem.getProductSku())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + reqItem.getProductSku()));

            // --- Inventory Check ---
            if (product.getCurrentStock() < reqItem.getQuantitySold()) {
                throw new IllegalStateException("Insufficient stock for product '" + product.getName() + "'. Available: " + product.getCurrentStock());
            }

            // --- Calculation ---
            double itemTotal = product.getSellingPricePerBaseUnit() * reqItem.getQuantitySold();
            runningSubtotal += itemTotal;

            // --- Stock Deduction (CRITICAL) ---
            product.setCurrentStock(product.getCurrentStock() - reqItem.getQuantitySold());
            productRepository.save(product); // Save updated stock (part of this transaction)

            // --- Create Audit Record (InvoiceItem) ---
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setQuantitySold(reqItem.getQuantitySold());
            item.setUnitPriceAtSale(product.getSellingPricePerBaseUnit());
            item.setLineTotal(itemTotal);
            item.setItemDiscount(0.0);

            items.add(item);
        }

        subTotalRef.set(runningSubtotal);
        return items;
    }


    /**
     * Maps the final saved Invoice entity to the InvoiceResponse DTO.
     */
    private InvoiceResponse mapToResponse(Invoice invoice) {
        String contact = invoice.getCustomer() != null ? invoice.getCustomer().getContactNumber() : null;
        String name = invoice.getCustomer() != null ? invoice.getCustomer().getName() : "Anonymous";

        List<InvoiceItemResponse> itemResponses = invoice.getItems().stream()
                .map(item -> new InvoiceItemResponse(
                        item.getProduct().getName(),
                        item.getProduct().getSku(),
                        item.getQuantitySold(),
                        item.getUnitPriceAtSale(),
                        item.getLineTotal()
                ))
                .collect(Collectors.toList());

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                name,
                contact,
                itemResponses,
                invoice.getSubTotal(),
                invoice.getTotalDiscount(),
                invoice.getTotalTax(),
                invoice.getGrandTotal()
        );
    }
}