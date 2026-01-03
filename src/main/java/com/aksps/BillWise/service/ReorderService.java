package com.aksps.BillWise.service;

import com.aksps.BillWise.model.ForecastResult;
import com.aksps.BillWise.model.ReorderSuggestion;
import com.aksps.BillWise.repository.ReorderSuggestionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReorderService {

    private final ReorderSuggestionRepository repo;

    public ReorderService(ReorderSuggestionRepository repo) {
        this.repo = repo;
    }

    public void evaluateReorder(ForecastResult forecast, int currentStock) {

        if (forecast == null) return;

        Integer predicted = forecast.getPredictedUnits();
        if (predicted == null) return;

        if (predicted <= currentStock) {
            return; // No reorder needed
        }

        int reorderQty = predicted - currentStock;

        ReorderSuggestion rs = new ReorderSuggestion();
        rs.setProductId(forecast.getProductId());
        rs.setForecastMonth(forecast.getForecastMonth());
        rs.setCurrentStock(currentStock);
        rs.setPredictedDemand(predicted);
        rs.setSuggestedReorderQty(reorderQty);
        rs.setCreatedAt(LocalDateTime.now());

        repo.save(rs);
    }

    // New API matching your requested signature
    public void evaluate(ForecastResult forecast, int currentStock) {
        evaluateReorder(forecast, currentStock);
    }

    // Added to satisfy existing controller's expectations.
    public static class Suggestion {
        public int suggestedQty;
        public LocalDate expectedArrival;
        public String reason;

        public Suggestion(int suggestedQty, LocalDate expectedArrival, String reason) {
            this.suggestedQty = suggestedQty;
            this.expectedArrival = expectedArrival;
            this.reason = reason;
        }
    }

    /**
     * Simple suggestReorder method used by InventoryController.
     * Logic: need = forecastedDemandInLeadTime - (onHand + pendingInbound)
     * Round up to packSize and ensure non-negative.
     */
    public Suggestion suggestReorder(int onHand, int pendingInbound, int packSize,
                                     double leadTimeDays, int forecastedDemandInLeadTime, double serviceLevel) {
        int available = onHand + pendingInbound;
        int need = forecastedDemandInLeadTime - available;
        if (need <= 0) {
            return new Suggestion(0, LocalDate.now().plusDays((long)Math.ceil(leadTimeDays)), "No reorder needed");
        }

        int suggested = need;
        if (packSize > 1) {
            int packs = (suggested + packSize - 1) / packSize;
            suggested = packs * packSize;
        }

        LocalDate expectedArrival = LocalDate.now().plusDays((long)Math.ceil(leadTimeDays));
        String reason = "Predicted demand exceeds available stock";
        return new Suggestion(suggested, expectedArrival, reason);
    }
}
