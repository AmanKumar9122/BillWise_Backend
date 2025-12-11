package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.response.ReplenishmentSuggestion;
import com.aksps.BillWise.service.ReplenishmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/replenishment")
public class ReplenishmentController {

    private final ReplenishmentService service;

    public ReplenishmentController(ReplenishmentService service) {
        this.service = service;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ReplenishmentSuggestion> getSuggestion(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(service.getSuggestion(productId));
    }
}
