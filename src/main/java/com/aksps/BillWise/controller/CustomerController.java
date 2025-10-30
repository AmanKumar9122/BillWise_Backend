package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.request.CustomerRequest;
import com.aksps.BillWise.dto.response.CustomerResponse;
import com.aksps.BillWise.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Feature: Quick lookup by contact number for pre-filling customer details.
     * Accessible by any authenticated user for speed at checkout.
     */
    @GetMapping("/lookup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerResponse> lookupCustomer(@RequestParam String contactNumber) {
        return customerService.getCustomerByContactNumber(contactNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all customer records (secured).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    /**
     * Creates a new customer record (secured).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerRequest customerRequest) {
        try {
            CustomerResponse createdCustomer = customerService.createCustomer(customerRequest);
            return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
