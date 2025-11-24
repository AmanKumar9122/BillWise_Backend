package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.ml.PredictionRequest;
import com.aksps.BillWise.dto.ml.PredictionResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MlIntegrationService {

    private final WebClient webClient;

    public MlIntegrationService(WebClient.Builder webClientBuilder,
                                @Value("${ml.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @CircuitBreaker(name = "mlService", fallbackMethod = "predictionFallback")
    @Retry(name = "mlService")
    @TimeLimiter(name = "mlService")
    public Mono<PredictionResponse> getSalesPrediction(Long request) {
        return webClient.post()
                .uri("/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PredictionResponse.class);
    }

    // Fallback must return same return type
    public Mono<PredictionResponse> predictionFallback(PredictionRequest request, Throwable ex) {
        return Mono.just(
                new PredictionResponse(0.0,
                        "Prediction Service Offline - Returned fallback value")
        );
    }
}
