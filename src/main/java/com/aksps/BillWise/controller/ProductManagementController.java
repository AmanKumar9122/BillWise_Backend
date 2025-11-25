package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.request.ProductRequest;
import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.exception.ResourceNotFoundException;
import com.aksps.BillWise.exception.ValidationException;
import com.aksps.BillWise.service.ProductManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin / Manager product management controller.
 * Base Path: /api/admin/products
 * Responsibilities:
 *  - Create product
 *  - Update product
 *  - Delete product
 *  - Admin-level listing and detail
 * Roles: ADMIN, MANAGER
 */
@RestController
@RequestMapping("/api/admin/products")
public class ProductManagementController {

    private final ProductManagementService productManagementService;

    public ProductManagementController(ProductManagementService productManagementService) {
        this.productManagementService = productManagementService;
    }

    /**
     * Creates a new product.
     * Endpoint: POST /api/admin/products
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse response = productManagementService.createProduct(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieves all products in the inventory.
     * Endpoint: GET /api/admin/products
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productManagementService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves a specific product by ID.
     * Endpoint: GET /api/admin/products/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            ProductResponse product = productManagementService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Updates an existing product.
     * Endpoint: PUT /api/admin/products/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse response = productManagementService.updateProduct(id, request);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException | ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Deletes a product by ID.
     * Endpoint: DELETE /api/admin/products/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productManagementService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
