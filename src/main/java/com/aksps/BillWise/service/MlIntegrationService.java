package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.ml.PredictionResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;

/**
 * Service responsible for integration with the external Machine Learning microservice
 * to fetch sales predictions, now with built-in resilience.
 */
@Service
public class MlIntegrationService {

    private static final String ML_SERVICE_CB = "mlPredictionService"; // Circuit breaker name

    private final WebClient webClient;

    public MlIntegrationService(WebClient.Builder webClientBuilder,
                                @Value("${ml.service.base-url:http://localhost:5000}") String mlBaseUrl) {
        // Use the provided base URL to build the client, no need to keep it as a field
        this.webClient = webClientBuilder.baseUrl(mlBaseUrl).build();
    }

    /**
     * Fetches the sales prediction for a given product ID using the external ML microservice.
     * If the remote call fails, Resilience4j CircuitBreaker will route to the fallback method.
     *
     * @param productId The ID of the product to forecast.
     * @return A Mono that will emit the structured PredictionResponse.
     */
    @CircuitBreaker(name = ML_SERVICE_CB, fallbackMethod = "salesPredictionFallback")
    public Mono<PredictionResponse> getSalesPrediction(Long productId) {
        // Call the ML service endpoint (relative to base URL set above)
        String url = "/forecast?product_id=" + productId;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(PredictionResponse.class)
                .timeout(Duration.ofSeconds(5))
                // if an error happens before circuit breaker triggers, route to fallback as well
                .onErrorResume(throwable -> salesPredictionFallback(productId, throwable));
    }

    /**
     * Fallback method for the getSalesPrediction Circuit Breaker.
     * Returns a default response indicating the service is unavailable.
     */
    public Mono<PredictionResponse> salesPredictionFallback(Long productId, Throwable t) {
        System.err.println("ML Integration Service is DOWN or TIMED OUT for product " + productId + ". Reason: " + (t == null ? "unknown" : t.getMessage()));

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