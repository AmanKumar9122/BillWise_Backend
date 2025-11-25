// Why this file: Core service for processing sales transactions, performing financial calculations,
// managing customer details, updating inventory atomically, and generating invoice records.

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.18); // 18% GST

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ProductRepository productRepository,
                          CustomerService customerService,
                          CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public InvoiceResponse createInvoice(InvoiceRequest request) {

        Customer customer = handleCustomer(request.getCustomerContactNumber(), request.getCustomerName());

        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(LocalDateTime.now());

        AtomicReference<BigDecimal> subTotalRef = new AtomicReference<>(BigDecimal.ZERO);
        List<InvoiceItem> items = processAndSaveItems(invoice, request.getItems(), subTotalRef);

        invoice.setItems(items);
        invoice.setSubTotal(subTotalRef.get());

        BigDecimal discountPercentage = request.getTotalDiscountPercentage() != null
                ? BigDecimal.valueOf(request.getTotalDiscountPercentage())
                : BigDecimal.ZERO;

        BigDecimal totalDiscountAmount = subTotalRef.get()
                .multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100));

        invoice.setTotalDiscount(totalDiscountAmount);

        BigDecimal taxableAmount = subTotalRef.get().subtract(totalDiscountAmount);
        BigDecimal totalTax = taxableAmount.multiply(TAX_RATE);
        invoice.setTotalTax(totalTax);

        invoice.setGrandTotal(taxableAmount.add(totalTax));

        invoice.setInvoiceNumber("INV-" + (invoiceRepository.count() + 1));

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return mapToResponse(savedInvoice);
    }

    private Customer handleCustomer(String contactNumber, String name) {

        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            return null;
        }

        Optional<Customer> existingCustomer = customerRepository.findByContactNumber(contactNumber);

        if (existingCustomer.isPresent()) {
            return existingCustomer.get();
        }

        CustomerRequest newCustomer = new CustomerRequest(
                name != null ? name : "Anonymous",
                contactNumber,
                null,
                null
        );

        customerService.createCustomer(newCustomer);

        return customerRepository.findByContactNumber(contactNumber)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve newly created customer."));
    }

    private List<InvoiceItem> processAndSaveItems(Invoice invoice,
                                                  List<InvoiceItemRequest> items,
                                                  AtomicReference<BigDecimal> subTotalRef) {

        List<InvoiceItem> invoiceItems = new ArrayList<>();
        BigDecimal runningTotal = BigDecimal.ZERO;

        for (InvoiceItemRequest req : items) {

            Product product = productRepository.findBySku(req.getProductSku())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + req.getProductSku()));

            if (product.getCurrentStock() < req.getQuantitySold()) {
                throw new IllegalStateException("Insufficient stock for " + product.getName());
            }

            BigDecimal lineTotal = product.getSellingPricePerBaseUnit()
                    .multiply(BigDecimal.valueOf(req.getQuantitySold()));

            runningTotal = runningTotal.add(lineTotal);

            product.setCurrentStock(product.getCurrentStock() - req.getQuantitySold());
            productRepository.save(product);

            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setQuantitySold(req.getQuantitySold());
            item.setUnitPriceAtSale(product.getSellingPricePerBaseUnit());
            item.setLineTotal(lineTotal);

            invoiceItems.add(item);
        }

        subTotalRef.set(runningTotal);
        return invoiceItems;
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {

        String contact = invoice.getCustomer() != null ? invoice.getCustomer().getContactNumber() : null;
        String name = invoice.getCustomer() != null ? invoice.getCustomer().getName() : "Anonymous";

        List<InvoiceItemResponse> itemDtos = invoice.getItems().stream()
                .map(i -> new InvoiceItemResponse(
                        i.getProduct().getName(),
                        i.getProduct().getSku(),
                        i.getQuantitySold(),
                        i.getUnitPriceAtSale(),
                        i.getLineTotal()))
                .collect(Collectors.toList());

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                name,
                contact,
                itemDtos,
                invoice.getSubTotal(),
                invoice.getTotalDiscount(),
                invoice.getTotalTax(),
                invoice.getGrandTotal()
        );
    }

    public InvoiceResponse getInvoiceResponseById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));

        return mapToResponse(invoice);
    }
}
