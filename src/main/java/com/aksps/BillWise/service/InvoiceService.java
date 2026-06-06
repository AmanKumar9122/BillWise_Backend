package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.response.InvoiceReportDetail;
import com.aksps.BillWise.dto.request.InvoiceFilterRequest;
import com.aksps.BillWise.dto.request.InvoiceItemRequest;
import com.aksps.BillWise.dto.request.InvoiceRequest;
import com.aksps.BillWise.dto.response.InvoiceItemResponse;
import com.aksps.BillWise.dto.response.InvoiceResponse;
import com.aksps.BillWise.exception.ResourceNotFoundException;
import com.aksps.BillWise.exception.ValidationException;
import com.aksps.BillWise.model.Customer;
import com.aksps.BillWise.model.Invoice;
import com.aksps.BillWise.model.InvoiceItem;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.repository.CustomerRepository;
import com.aksps.BillWise.repository.InvoiceRepository;
import com.aksps.BillWise.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InvoicePdfService invoicePdfService;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int DECIMAL_SCALE = 2;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ProductRepository productRepository,
                          CustomerRepository customerRepository,
                          InvoicePdfService invoicePdfService) {

        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.invoicePdfService = invoicePdfService;
    }


    // ⭐ AUTO FETCH or AUTO CREATE CUSTOMER
    private Customer resolveCustomer(InvoiceRequest request) {

        return customerRepository.findByContactNumber(request.contactNumber())
                .orElseGet(() -> {

                    Customer c = new Customer(
                            null,
                            request.customerName(),
                            request.contactNumber(),
                            request.email(),
                            request.gstNumber()
                    );

                    return customerRepository.save(c);
                });
    }


    // -------------------------------------------------------------
    // ⭐ CREATE INVOICE (Transactional)
    // -------------------------------------------------------------
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {

        // 1. Create or fetch customer
        Customer customer = resolveCustomer(request);

        // 2. Build invoice header
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(UUID.randomUUID().toString());
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setCustomer(customer);      // new: linked customer entity
        invoice.setCustomerName(customer.getName());  // still stored for history

        List<InvoiceItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        // 3. Process invoice line items
        for (InvoiceItemRequest itemReq : request.items()) {

            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with ID: " + itemReq.productId()
                    ));

            if (product.getCurrentStock() < itemReq.quantity()) {
                throw new ValidationException(
                        "Insufficient stock for '" + product.getName() + "'. " +
                                "Available: " + product.getCurrentStock() +
                                ", Requested: " + itemReq.quantity()
                );
            }

            // Deduct stock
            product.setCurrentStock(product.getCurrentStock() - itemReq.quantity());
            productRepository.save(product);

            // Financial calculation
            BigDecimal unitPrice = product.getSellingPricePerBaseUnit();
            BigDecimal qty = BigDecimal.valueOf(itemReq.quantity());

            BigDecimal lineTotalBeforeDiscount = unitPrice.multiply(qty)
                    .setScale(DECIMAL_SCALE, ROUNDING_MODE);

            BigDecimal itemDiscount = BigDecimal.ZERO;
            BigDecimal lineTotal = lineTotalBeforeDiscount.subtract(itemDiscount);

            subtotal = subtotal.add(lineTotal);

            // Create invoice item
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setQuantitySold(itemReq.quantity());
            item.setUnitPriceAtSale(unitPrice);
            item.setItemDiscount(itemDiscount);
            item.setLineTotal(lineTotal);

            items.add(item);
        }

        // 4. Tax & totals
        BigDecimal taxRate = BigDecimal.valueOf(request.taxRate());
        BigDecimal totalTax = subtotal.multiply(taxRate).setScale(DECIMAL_SCALE, ROUNDING_MODE);

        BigDecimal grandTotal = subtotal.add(totalTax).setScale(DECIMAL_SCALE, ROUNDING_MODE);

        invoice.setSubTotal(subtotal);
        invoice.setTotalDiscount(totalDiscount);
        invoice.setTotalTax(totalTax);
        invoice.setGrandTotal(grandTotal);
        invoice.setItems(items);

        // 5. Save invoice
        Invoice saved = invoiceRepository.save(invoice);

        return mapToInvoiceResponse(saved);
    }


    // -------------------------------------------------------------
    // ⭐ GET SINGLE INVOICE
    // -------------------------------------------------------------
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found with ID: " + id));

        return mapToInvoiceResponse(invoice);
    }


    // -------------------------------------------------------------
    // ⭐ FILTERED + PAGINATED INVOICE LIST
    // -------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getInvoices(
            InvoiceFilterRequest filter,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                direction.equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy
        );

        LocalDateTime startDate = filter.startDate() != null ?
                filter.startDate().atStartOfDay() : null;

        LocalDateTime endDate = filter.endDate() != null ?
                filter.endDate().atTime(23, 59, 59) : null;

        boolean hasFilters = filter.customerName() != null || startDate != null || endDate != null;
        Page<Invoice> invoices = hasFilters
                ? invoiceRepository.searchInvoices(filter.customerName(), startDate, endDate, pageable)
                : invoiceRepository.findAll(pageable);

        return invoices.map(this::mapToInvoiceResponse);
    }


    // -------------------------------------------------------------
    // ⭐ Mapping Helpers
    // -------------------------------------------------------------
    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {

        List<InvoiceItem> itemsList = invoice.getItems() != null ? invoice.getItems() : Collections.<InvoiceItem>emptyList();

        List<InvoiceItemResponse> items = itemsList.stream()
                .map(this::mapToInvoiceItemResponse)
                .collect(Collectors.toList());

        BigDecimal subTotal = invoice.getSubTotal() != null ? invoice.getSubTotal() : BigDecimal.ZERO;
        BigDecimal totalDiscount = invoice.getTotalDiscount() != null ? invoice.getTotalDiscount() : BigDecimal.ZERO;
        BigDecimal totalTax = invoice.getTotalTax() != null ? invoice.getTotalTax() : BigDecimal.ZERO;
        BigDecimal grandTotal = invoice.getGrandTotal() != null ? invoice.getGrandTotal() : BigDecimal.ZERO;

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getCustomerName(),
                subTotal,
                totalDiscount,
                totalTax,
                grandTotal,
                items
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

    @Transactional(readOnly = true)
    public InvoiceReportDetail getPrintableInvoice(Long invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        // Customer details (optional if customer entity is not linked)
        String customerName = invoice.getCustomerName();
        String customerContact = null;
        String customerEmail = null;
        String customerGst = null;

        if (invoice.getCustomer() != null) {
            customerName = invoice.getCustomer().getName();
            customerContact = invoice.getCustomer().getContactNumber();
            customerEmail = invoice.getCustomer().getEmail();
            customerGst = invoice.getCustomer().getGstNumber();
        }

        List<InvoiceReportDetail.ReportItem> lineItems =
                (invoice.getItems() != null ? invoice.getItems() : Collections.<InvoiceItem>emptyList()).stream().map(i -> new InvoiceReportDetail.ReportItem(
                        i.getProduct().getName(),
                        i.getProduct().getSku(),
                        i.getQuantitySold(),
                        i.getUnitPriceAtSale(),
                        i.getLineTotal(),
                        i.getItemDiscount()
                )).toList();

        BigDecimal subTotal = invoice.getSubTotal() != null ? invoice.getSubTotal() : BigDecimal.ZERO;
        BigDecimal totalTax = invoice.getTotalTax() != null ? invoice.getTotalTax() : BigDecimal.ZERO;

        BigDecimal taxRatePercent = BigDecimal.ZERO;
        if (subTotal.compareTo(BigDecimal.ZERO) > 0) {
            taxRatePercent = totalTax.divide(subTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new InvoiceReportDetail(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),

                customerName,
                customerContact,
                customerEmail,
                customerGst,

                "BillWise Store",
                "Purani Godown, Gaya, Bihar",
                "+91 9122488130",

                lineItems,

                taxRatePercent,
                subTotal,
                invoice.getTotalDiscount() != null ? invoice.getTotalDiscount() : BigDecimal.ZERO,
                totalTax,
                invoice.getGrandTotal() != null ? invoice.getGrandTotal() : BigDecimal.ZERO
        );

    }

    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long id) {
        InvoiceReportDetail report = getPrintableInvoice(id);
        return invoicePdfService.generatePdf(report);
    }


}
