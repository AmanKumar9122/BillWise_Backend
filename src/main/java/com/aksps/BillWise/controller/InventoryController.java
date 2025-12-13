package com.aksps.BillWise.controller;

import com.aksps.BillWise.dto.response.ForecastResponse;
import com.aksps.BillWise.model.ReorderOverride;
import com.aksps.BillWise.service.ReorderOverrideService;
import com.aksps.BillWise.service.ReorderService;
import com.aksps.BillWise.service.ReorderService.Suggestion;
import com.aksps.BillWise.service.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final ReorderService reorderService;
    private final ForecastService forecastService;
    private final ReorderOverrideService overrideService;

    public InventoryController(ReorderService reorderService, ForecastService forecastService, ReorderOverrideService overrideService) {
        this.reorderService = reorderService;
        this.forecastService = forecastService;
        this.overrideService = overrideService;
    }

    @PostMapping("/suggest-reorder")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> suggestReorder(@RequestBody SuggestRequest req) {
        Suggestion s = reorderService.suggestReorder(req.onHand, req.pendingInbound, req.packSize, req.leadTimeDays, req.forecastedDemandInLeadTime, req.serviceLevel);
        return ResponseEntity.ok(new SuggestResponse(s.suggestedQty, s.expectedArrival.toString(), s.reason));
    }

    @PostMapping("/suggest-reorder-with-forecast")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> suggestReorderWithForecast(@RequestBody SuggestWithForecastRequest req) {
        // fetch forecast for product and sum predicted units for lead time (months approximation)
        ForecastResponse forecast = forecastService.getForecast(req.productId, req.months);
        int forecastedDemand = 0;
        if (forecast != null && forecast.getDailyPredictions() != null) {
            // If frequency is monthly, dailyPredictions contains period entries (we treat as units per period)
            int take = Math.min(req.months, forecast.getDailyPredictions().size());
            for (int i = 0; i < take; i++) {
                forecastedDemand += forecast.getDailyPredictions().get(i).getPredictedUnits();
            }
        }

        Suggestion s = reorderService.suggestReorder(req.onHand, req.pendingInbound, req.packSize, req.leadTimeDays, forecastedDemand, req.serviceLevel);
        return ResponseEntity.ok(new SuggestResponse(s.suggestedQty, s.expectedArrival.toString(), s.reason));
    }

    @PostMapping("/override")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> saveOverride(@RequestBody OverrideRequest req) {
        ReorderOverride ov = new ReorderOverride();
        ov.setProductId(req.productId);
        ov.setUserId(req.userId);
        ov.setSuggestedQty(req.suggestedQty);
        ov.setFinalQty(req.finalQty);
        ov.setReason(req.reason);
        ReorderOverride saved = overrideService.saveOverride(ov);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/override/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> getOverrides(@PathVariable Long productId) {
        List<ReorderOverride> list = overrideService.getOverridesForProduct(productId);
        return ResponseEntity.ok(list);
    }

    public static class SuggestRequest {
        public int onHand;
        public int pendingInbound;
        public int packSize;
        public double leadTimeDays;
        public int forecastedDemandInLeadTime;
        public double serviceLevel = 0.95;
    }

    public static class SuggestWithForecastRequest {
        public Long productId;
        public int months = 1; // horizon for forecast
        public int onHand;
        public int pendingInbound;
        public int packSize;
        public double leadTimeDays;
        public double serviceLevel = 0.95;
    }

    public static class OverrideRequest {
        public Long productId;
        public Long userId;
        public int suggestedQty;
        public int finalQty;
        public String reason;
    }

    public static class SuggestResponse {
        public int suggestedQty;
        public String expectedArrival;
        public String reason;

        public SuggestResponse(int suggestedQty, String expectedArrival, String reason) {
            this.suggestedQty = suggestedQty;
            this.expectedArrival = expectedArrival;
            this.reason = reason;
        }
    }
}
