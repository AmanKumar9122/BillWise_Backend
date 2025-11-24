package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST Controller exposing analytics endpoints.
 * Accessible only to MANAGER and ADMIN users.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET endpoint for retrieving real-time low stock product alerts.
     * Intended for dashboard display.
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ProductResponse>> getLowStockAlerts() {
        return ResponseEntity.ok(analyticsService.getLowStockProducts());
    }

    /**
     * GET endpoint for predicted sales using ML forecasting.
     * Calls asynchronous reactive ML service.
     */
    @GetMapping("/forecast/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Mono<Object> predictSales(@PathVariable Long productId) {
        return analyticsService.getPredictedSales(productId);
    }
}
