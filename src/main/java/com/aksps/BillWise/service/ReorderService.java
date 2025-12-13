package com.aksps.BillWise.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReorderService {

    // Simple z-score table for common service levels
    private static final Map<Double, Double> Z_TABLE = new HashMap<>();
    static {
        Z_TABLE.put(0.90, 1.28);
        Z_TABLE.put(0.95, 1.65);
        Z_TABLE.put(0.98, 2.05);
        Z_TABLE.put(0.99, 2.33);
    }

    public static class Suggestion {
        public int suggestedQty;
        public LocalDate expectedArrival;
        public String reason;
    }

    public Suggestion suggestReorder(int onHand, int pendingInbound, int packSize, double leadTimeDays, int forecastedDemandInLeadTime, double serviceLevel) {
        Suggestion s = new Suggestion();

        double z = Z_TABLE.getOrDefault(serviceLevel, 1.65);
        // rough sigma estimate: assume Poisson-like demand => sigma = sqrt(mean)
        double sigmaDaily = Math.sqrt(Math.max(1, (double)forecastedDemandInLeadTime / Math.max(1, (int)leadTimeDays)));
        double safetyStock = z * sigmaDaily * Math.sqrt(leadTimeDays);

        double raw = forecastedDemandInLeadTime + safetyStock - onHand + pendingInbound;
        int qty = (int)Math.max(0, Math.ceil(raw));
        // round up to pack size
        if (packSize > 1 && qty % packSize != 0) {
            qty = ((qty / packSize) + 1) * packSize;
        }

        s.suggestedQty = qty;
        s.expectedArrival = LocalDate.now().plusDays((long)Math.ceil(leadTimeDays));
        s.reason = String.format("forecast=%d, safetyStock=%.2f, onHand=%d, pending=%d", forecastedDemandInLeadTime, safetyStock, onHand, pendingInbound);
        return s;
    }
}

