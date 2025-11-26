package com.aksps.BillWise.controller;

import com.aksps.BillWise.model.ProductAuditLog;
import com.aksps.BillWise.repository.ProductAuditLogRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products/audit")
public class ProductAuditController {

    private final ProductAuditLogRepository repo;

    public ProductAuditController(ProductAuditLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductAuditLog> getHistory(@PathVariable Long productId) {
        return repo.findByProductIdOrderByTimestampDesc(productId);
    }
}
