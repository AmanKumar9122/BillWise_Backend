package com.aksps.BillWise.dto.ml;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing the structured sales prediction response from the ML microservice.
 * This DTO is comprehensive, including daily forecast data.
 */
public record PredictionResponse(
        Long productId,
        LocalDate predictionDate,
        String timeHorizon, // e.g., "30 days"
        BigDecimal predictedTotalSales, // The sum total predicted sales value
        List<DailyPrediction> dailyForecast
) {
    /**
     * Inner record for detailed daily forecast data.
     */
    public record DailyPrediction(
            LocalDate date,
            Integer predictedUnits,
            BigDecimal predictedRevenue
    ) {}
}