package com.aksps.BillWise.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ForecastResultRequest {

    private Long productId;
    private LocalDate forecastMonth;
    private Integer predictedUnits;
    private Double predictedRevenue;
    private String modelType;
    private LocalDateTime generatedAt;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDate getForecastMonth() {
        return forecastMonth;
    }

    public void setForecastMonth(LocalDate forecastMonth) {
        this.forecastMonth = forecastMonth;
    }

    public Integer getPredictedUnits() {
        return predictedUnits;
    }

    public void setPredictedUnits(Integer predictedUnits) {
        this.predictedUnits = predictedUnits;
    }

    public Double getPredictedRevenue() {
        return predictedRevenue;
    }

    public void setPredictedRevenue(Double predictedRevenue) {
        this.predictedRevenue = predictedRevenue;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}

