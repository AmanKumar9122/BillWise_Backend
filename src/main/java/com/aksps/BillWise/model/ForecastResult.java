package com.aksps.BillWise.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "forecast_results",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "forecast_month"})
)
public class ForecastResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "forecast_month", nullable = false)
    private LocalDate forecastMonth;

    @Column(name = "predicted_units")
    private Integer predictedUnits;

    @Column(name = "predicted_revenue")
    private Double predictedRevenue;

    @Column(name = "model_type")
    private String modelType;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "created_by")
    private String createdBy = "ML_SERVICE";

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
