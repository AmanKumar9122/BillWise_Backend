package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.ml.PredictionResponse;
import com.aksps.BillWise.dto.response.ForecastResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for integration with the external Machine Learning microservice
 * to fetch sales predictions, now with built-in resilience.
 */
@Service
public class MlIntegrationService {

    private static final String ML_SERVICE_CB = "mlPredictionService"; // Circuit breaker name

    private final WebClient webClient;
    private final ForecastHistoryService historyService;

    public MlIntegrationService(WebClient.Builder webClientBuilder,
                                ForecastHistoryService historyService,
                                @Value("${ml.service.base-url:http://localhost:5000}") String mlBaseUrl) {
        // Use the provided base URL to build the client, no need to keep it as a field
        this.webClient = webClientBuilder.baseUrl(mlBaseUrl).build();
        this.historyService = historyService;
    }

    /**
     * Fetches the sales prediction for a given product ID using the external ML microservice.
     * If the remote call fails, Resilience4j CircuitBreaker will route to the fallback method.
     *
     * @param productId The ID of the product to forecast.
     * @return A Mono that will emit the structured PredictionResponse.
     */
    @CircuitBreaker(name = ML_SERVICE_CB, fallbackMethod = "salesPredictionFallback")
    public Mono<PredictionResponse> getSalesPrediction(Long productId, int months) {
        // Call the ML service endpoint (relative to base URL set above)
        String url = "/forecast?product_id=" + productId + "&months=" + months;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(PredictionResponse.class)
                .timeout(Duration.ofSeconds(5))
                // if an error happens before circuit breaker triggers, route to fallback as well
                .onErrorResume(throwable -> salesPredictionFallback(productId, months, throwable));
    }

    /**
     * Fallback method for the getSalesPrediction Circuit Breaker.
     * Returns last persisted forecast from ForecastHistoryService if available.
     */
    public Mono<PredictionResponse> salesPredictionFallback(Long productId, int months, Throwable t) {
        System.err.println("ML Integration Service is DOWN or TIMED OUT for product " + productId + ". Reason: " + (t == null ? "unknown" : t.getMessage()));

        ForecastResponse last = historyService.getLatestForecastForProduct(productId, months);
        if (last != null && last.getDailyPredictions() != null && !last.getDailyPredictions().isEmpty()) {
            // Map ForecastResponse -> PredictionResponse
            List<PredictionResponse.DailyPrediction> daily = new ArrayList<>();
            last.getDailyPredictions().forEach(d -> {
                try {
                    LocalDate date = LocalDate.parse(d.getDate());
                    daily.add(new PredictionResponse.DailyPrediction(date, d.getPredictedUnits(), BigDecimal.valueOf(d.getPredictedRevenue())));
                } catch (Exception ignored) {}
            });

            PredictionResponse pr = new PredictionResponse(
                    last.getProductId(),
                    last.getGeneratedAt() != null ? last.getGeneratedAt() : LocalDate.now(),
                    last.getForecastingWindow() != null ? last.getForecastingWindow() : "Fallback",
                    BigDecimal.valueOf(last.getPredictedTotalRevenue() != null ? last.getPredictedTotalRevenue() : 0.0),
                    daily
            );

            return Mono.just(pr);
        }

        // No persisted forecast available, return empty default
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
