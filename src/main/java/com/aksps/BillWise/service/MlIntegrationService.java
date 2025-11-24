package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.ml.PredictionResponse;
import com.aksps.BillWise.dto.ml.PredictionResponse.DailyPrediction;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker; // Added Resilience4j import
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for integration with the external Machine Learning microservice
 * to fetch sales predictions, now with built-in resilience.
 */
@Service
public class MlIntegrationService {

    private static final String ML_SERVICE_CB = "mlPredictionService"; // Circuit breaker name

    // You would typically inject WebClient or WebClient.Builder here

    /**
     * Fetches the sales prediction for a given product ID.
     *
     * @param productId The ID of the product to forecast.
     * @return A Mono that will emit the structured PredictionResponse.
     */
    @CircuitBreaker(name = ML_SERVICE_CB, fallbackMethod = "salesPredictionFallback")
    public Mono<PredictionResponse> getSalesPrediction(Long productId) {
        // --- Placeholder Implementation for Demonstration ---
        // Simulating a network call delay and response creation
        return Mono.delay(Duration.ofMillis(200))
                .map(delay -> {
                    // ** Simulate a temporary failure for testing the circuit breaker **
                    // if (System.currentTimeMillis() % 10 < 2) {
                    //    throw new RuntimeException("ML Service timed out or failed!");
                    // }

                    LocalDate today = LocalDate.now();

                    // Create mock daily forecast data compliant with the DTO structure
                    List<DailyPrediction> dailyForecast = List.of(
                            new DailyPrediction(today.plusDays(1), 150, new BigDecimal("1500.00")),
                            new DailyPrediction(today.plusDays(2), 160, new BigDecimal("1600.00")),
                            new DailyPrediction(today.plusDays(3), 145, new BigDecimal("1450.00"))
                    );

                    // Calculate total sales from the mock data
                    BigDecimal predictedTotalSales = dailyForecast.stream()
                            .map(DailyPrediction::predictedRevenue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Correctly instantiate the PredictionResponse
                    return new PredictionResponse(
                            productId,
                            today,
                            "3 Days",
                            predictedTotalSales,
                            dailyForecast
                    );
                });
    }

    /**
     * Fallback method for the getSalesPrediction Circuit Breaker.
     * Returns a default response indicating the service is unavailable.
     */
    public Mono<PredictionResponse> salesPredictionFallback(Long productId, Throwable t) {
        System.err.println("ML Integration Service is DOWN or TIMED OUT for product " + productId + ". Reason: " + t.getMessage());

        // Return an empty/safe default prediction response
        PredictionResponse fallbackResponse = new PredictionResponse(
                productId,
                LocalDate.now(),
                "Fallback Data",
                BigDecimal.ZERO,
                Collections.emptyList() // No daily data available
        );

        return Mono.just(fallbackResponse);
    }
}