package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.ml.PredictionResponse;
import com.aksps.BillWise.dto.response.ForecastResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class MlAdapter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public ForecastResponse toForecastResponse(PredictionResponse pred, int months) {
        ForecastResponse resp = new ForecastResponse();
        if (pred == null) {
            return resp;
        }

        resp.setProductId(pred.productId());
        resp.setGeneratedAt(pred.predictionDate());
        resp.setForecastingWindow(pred.timeHorizon());
        // Use predictedTotalSales -> map to predictedTotalRevenue (approximate)
        resp.setPredictedTotalRevenue(pred.predictedTotalSales() != null ? pred.predictedTotalSales().doubleValue() : 0.0);

        List<ForecastResponse.DailyPrediction> daily = new ArrayList<>();
        if (pred.dailyForecast() != null) {
            for (PredictionResponse.DailyPrediction d : pred.dailyForecast()) {
                ForecastResponse.DailyPrediction dp = new ForecastResponse.DailyPrediction();
                dp.setDate(d.date() != null ? d.date().format(DATE_FMT) : null);
                dp.setPredictedUnits(d.predictedUnits());
                dp.setPredictedRevenue(d.predictedRevenue() != null ? d.predictedRevenue().doubleValue() : 0.0);
                daily.add(dp);
            }
        }
        resp.setDailyPredictions(daily);
        return resp;
    }

    /**
     * Detect frequency from prediction payload.
     * Returns "M" for monthly, "D" for daily, or null if unknown.
     */
    public String detectFrequency(PredictionResponse pred) {
        if (pred == null || pred.dailyForecast() == null || pred.dailyForecast().isEmpty()) return null;
        try {
            // If timeHorizon contains 'month' treat as monthly
            if (pred.timeHorizon() != null && pred.timeHorizon().toLowerCase().contains("month")) {
                return "M";
            }
            // Inspect dates: if all dates are first-of-month or have day=1 -> monthly
            boolean allFirstOfMonth = true;
            boolean hasDailySpacing = false;
            LocalDate prev = null;
            for (PredictionResponse.DailyPrediction d : pred.dailyForecast()) {
                LocalDate dt = d.date();
                if (dt.getDayOfMonth() != 1) {
                    allFirstOfMonth = false;
                }
                if (prev != null) {
                    if (!dt.equals(prev.plusDays(1))) {
                        // not contiguous daily
                        hasDailySpacing = true;
                    }
                }
                prev = dt;
            }
            if (allFirstOfMonth) return "M";
            if (!hasDailySpacing) return "D";
        } catch (Exception ignored) {}
        return null;
    }
}
