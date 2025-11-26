package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public / general product access controller.
 * Base Path: /api/products
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * NEW:
     * GET /api/products?search=&page=&size=&sortBy=&direction=
     *
     * Public API (no login)
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProductsPaged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction
    ) {
        Page<ProductResponse> result = productService.getProductsPaged(
                search, page, size, sortBy, direction
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieve single product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
