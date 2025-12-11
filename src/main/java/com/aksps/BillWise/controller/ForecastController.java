package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.response.ForecastResponse;
import com.aksps.BillWise.service.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    /**
     * GET /api/forecast/{skuId}?days=30
     * Returns ML forecast for the given SKU.
     */
    @GetMapping("/{skuId}")
    public ResponseEntity<ForecastResponse> getForecast(
            @PathVariable Long skuId,
            @RequestParam(defaultValue = "30") int days
    ) {
        ForecastResponse response = forecastService.getForecast(skuId, days);
        return ResponseEntity.ok(response);
    }
}
