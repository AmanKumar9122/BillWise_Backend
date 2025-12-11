package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.response.ForecastResponse;
import com.aksps.BillWise.dto.response.ReplenishmentSuggestion;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReplenishmentService {

    private final ProductRepository productRepository;
    private final ForecastService forecastService;

    public ReplenishmentService(ProductRepository productRepository,
                                ForecastService forecastService) {
        this.productRepository = productRepository;
        this.forecastService = forecastService;
    }

    public ReplenishmentSuggestion getSuggestion(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ForecastResponse forecast = forecastService.getForecast(productId, 1);

        // Handle empty or null forecast safely 🔥
        int predictedUnitsNextMonth = Optional.ofNullable(forecast.getDailyPredictions())
                .orElseGet(java.util.Collections::emptyList)
                .stream()
                .mapToInt(ForecastResponse.DailyPrediction::getPredictedUnits)
                .sum();

        int currentStock = Optional.ofNullable(product.getCurrentStock()).orElse(0);
        int minStock = Optional.ofNullable(product.getMinStockLevel()).orElse(0);
        int leadTime = Optional.ofNullable(product.getLeadTimeDays()).orElse(0);

        // Avoid division by zero
        int dailyDemand = predictedUnitsNextMonth > 0 ? predictedUnitsNextMonth / 30 : 0;

        int daysUntilStockout = dailyDemand > 0
                ? currentStock / dailyDemand
                : Integer.MAX_VALUE; // No demand = no stockout

        // Calculate reorder quantity
        int reorderQuantity = (predictedUnitsNextMonth + minStock) - currentStock;
        reorderQuantity = Math.max(reorderQuantity, 0);

        // Reorder decision
        String reorderDate;
        if (dailyDemand == 0) {
            reorderDate = "No demand — No need to order now";
        } else if (daysUntilStockout <= leadTime) {
            reorderDate = "ORDER NOW";
        } else {
            reorderDate = "Order in " + (daysUntilStockout - leadTime) + " days";
        }

        // Stockout risk score (0–100)
        int risk = (currentStock == 0)
                ? 100
                : Math.min(100, (minStock * 100) / Math.max(currentStock, 1));

        return new ReplenishmentSuggestion(
                productId,
                currentStock,
                predictedUnitsNextMonth,
                reorderQuantity,
                reorderDate,
                daysUntilStockout,
                risk
        );
    }
}
