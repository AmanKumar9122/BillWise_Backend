package com.aksps.BillWise.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing the structured sales prediction response from the ML microservice.
 * This DTO is comprehensive, including daily forecast data.
 */
public record PredictionResponse(
        Long productId,
        @JsonProperty("generatedAt") LocalDate predictionDate,
        @JsonProperty("forecastingWindow") String timeHorizon,
        @JsonProperty("predictedTotalRevenue") BigDecimal predictedTotalSales,
        @JsonProperty("dailyPredictions") List<DailyPrediction> dailyForecast
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
