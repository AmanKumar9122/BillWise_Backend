package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.request.StockAdjustmentRequest;
import com.aksps.BillWise.model.StockAdjustment;
import com.aksps.BillWise.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        inventoryService.adjustStock(request);
        return ResponseEntity.ok("Stock adjusted successfully.");
    }

    @GetMapping("/adjustments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<StockAdjustment>> getAdjustments() {
        return ResponseEntity.ok(inventoryService.getAllAdjustments());
    }
}
