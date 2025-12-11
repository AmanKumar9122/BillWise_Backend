package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.ml.SalesDataPoint;
import com.aksps.BillWise.service.AISalesDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller that exposes aggregated historical sales data for ML consumption.
 * Endpoint:
 *   GET /api/ai/monthly-sales/{productId}
 *
 * Returns a list of SalesDataPoint (date format: "YYYY-MM", unitsSold).
 */
@RestController
@RequestMapping("/api/ai")
public class AISalesDataController {

    private final AISalesDataService aiSalesDataService;

    public AISalesDataController(AISalesDataService aiSalesDataService) {
        this.aiSalesDataService = aiSalesDataService;
    }

    /**
     * Returns monthly aggregated sales for the given product.
     *
     * Example response:
     * [
     *   { "date": "2024-01", "unitsSold": 120 },
     *   { "date": "2024-02", "unitsSold": 98 }
     * ]
     *
     * This endpoint is used by the ML microservice to pull training data.
     */
    @GetMapping("/monthly-sales/{productId}")
    public ResponseEntity<List<SalesDataPoint>> getMonthlySales(@PathVariable Long productId) {
        try {
            List<SalesDataPoint> data = aiSalesDataService.getMonthlySales(productId);
            if (data == null || data.isEmpty()) {
                // return 204 No Content if no historical data exists for this product
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(data);
        } catch (RuntimeException ex) {
            // Likely product not found or other service-level error
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(List.of());
        } catch (Exception ex) {
            // Generic server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }
}
