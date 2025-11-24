package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.ml.PredictionResponse;
import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.dto.response.TopProductResponse;
import com.aksps.BillWise.service.AnalyticsService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST Controller exposing analytics and reporting APIs.
 * Includes:
 *  - Low Stock Alerts
 *  - ML-based Sales Forecasting
 *  - Top-selling product insights
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Returns list of low stock alerts for dashboard use.
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ProductResponse>> getLowStockAlerts() {
        return ResponseEntity.ok(analyticsService.getLowStockProducts());
    }

    /**
     * Calls ML forecasting service to predict near future demand for a product.
     * Fully reactive, non-blocking endpoint returning typed PredictionResponse.
     */
    @GetMapping("/forecast/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Mono<PredictionResponse> predictSales(@PathVariable Long productId) {
        return analyticsService.getPredictedSales(productId);
    }

    /**
     * Returns top-selling products ranked by revenue for given time period.
     */
    @GetMapping("/top-selling")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<TopProductResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(analyticsService.getTopSellingProducts(days));
    }
}
