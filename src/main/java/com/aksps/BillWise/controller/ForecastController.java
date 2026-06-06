package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.response.ForecastResponse;
import com.aksps.BillWise.service.ForecastScheduler;
import com.aksps.BillWise.service.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    private final ForecastService forecastService;
    private final ForecastScheduler forecastScheduler;

    public ForecastController(ForecastService forecastService, ForecastScheduler forecastScheduler) {
        this.forecastService = forecastService;
        this.forecastScheduler = forecastScheduler;
    }

    /**
     * GET /api/forecast/{skuId}?months=3
     * Returns ML forecast for the given SKU.
     */
    @GetMapping("/{skuId}")
    public ResponseEntity<ForecastResponse> getForecast(
            @PathVariable Long skuId,
            @RequestParam(defaultValue = "3") int months
    ) {
        ForecastResponse response = forecastService.getForecast(skuId, months);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> triggerBatchForecast() {
        // Run scheduler in a separate thread to avoid blocking the HTTP request
        new Thread(() -> forecastScheduler.runDailyForecasts()).start();
        return ResponseEntity.accepted().body("Batch forecast job started");
    }
}
