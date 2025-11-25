package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public / general product access controller.
 * Base Path: /api/products
 * Responsibilities:
 *  - List all products
 *  - Get product by ID
 * Roles: PUBLIC (no login required)
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Retrieves a list of all products.
     * Public API (no authentication required)
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves a single product by its ID.
     * Public API (no authentication required)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        try {
            ProductResponse product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
