package com.aksps.BillWise.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ForecastResponse {

    private Long productId;
    private LocalDate generatedAt;
    private String forecastingWindow;
    private Double predictedTotalRevenue;
    private List<DailyPrediction> dailyPredictions;

    @Data
    public static class DailyPrediction {
        private String date;
        private Integer predictedUnits;
        private Double predictedRevenue;
    }
}
