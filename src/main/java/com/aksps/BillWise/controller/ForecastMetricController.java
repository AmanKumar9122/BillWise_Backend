package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.request.ForecastMetricRequest;
import com.aksps.BillWise.service.ForecastMetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class ForecastMetricController {

    private final ForecastMetricService service;

    public ForecastMetricController(ForecastMetricService service) {
        this.service = service;
    }

    @PostMapping("/forecast-metrics")
    public ResponseEntity<Void> saveMetrics(@Valid @RequestBody ForecastMetricRequest req) {
        service.save(req);
        return ResponseEntity.ok().build();
    }
}
