package com.aksps.BillWise.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reorder_suggestions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id","forecast_month"}))
public class ReorderSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "forecast_month", nullable = false)
    private LocalDate forecastMonth;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock;

    @Column(name = "predicted_demand", nullable = false)
    private Integer predictedDemand;

    @Column(name = "suggested_reorder_qty", nullable = false)
    private Integer suggestedReorderQty;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "status")
    private String status = "PENDING";

    // getters & setters

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

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getPredictedDemand() {
        return predictedDemand;
    }

    public void setPredictedDemand(Integer predictedDemand) {
        this.predictedDemand = predictedDemand;
    }

    public Integer getSuggestedReorderQty() {
        return suggestedReorderQty;
    }

    public void setSuggestedReorderQty(Integer suggestedReorderQty) {
        this.suggestedReorderQty = suggestedReorderQty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

