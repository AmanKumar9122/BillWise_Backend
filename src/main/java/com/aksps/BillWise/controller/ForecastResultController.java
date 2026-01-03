package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.request.ForecastResultRequest;
import com.aksps.BillWise.service.ForecastResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class ForecastResultController {

    private final ForecastResultService service;

    public ForecastResultController(ForecastResultService service) {
        this.service = service;
    }

    @PostMapping("/forecast-results")
    public ResponseEntity<Void> saveForecast(@RequestBody ForecastResultRequest req) {
        service.save(req);
        return ResponseEntity.ok().build();
    }
}

