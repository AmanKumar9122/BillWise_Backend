package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.ml.PredictionResponse;
import com.aksps.BillWise.dto.response.ForecastResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ForecastService {

    private final MlIntegrationService mlIntegrationService;
    private final ForecastHistoryService historyService;
    private final MlAdapter mlAdapter;

    public ForecastService(MlIntegrationService mlIntegrationService,
                           ForecastHistoryService historyService,
                           MlAdapter mlAdapter) {
        this.mlIntegrationService = mlIntegrationService;
        this.historyService = historyService;
        this.mlAdapter = mlAdapter;
    }

    public ForecastResponse getForecast(Long productId, int months) {

        // Call ML service via MlIntegrationService which returns a reactive Mono<PredictionResponse>
        try {
            Mono<PredictionResponse> mono = mlIntegrationService.getSalesPrediction(productId, months);
            PredictionResponse pred = mono.block(Duration.ofSeconds(6)); // small blocking window

            ForecastResponse response = mlAdapter.toForecastResponse(pred, months);

            // Detect frequency (D=Daily, M=Monthly)
            String frequency = mlAdapter.detectFrequency(pred);

            // Save forecast to DB with frequency (modelVersion left null for now)
            historyService.saveForecast(productId, months, response, null, frequency);

            return response;
        } catch (Exception e) {
            // On error, try to return last persisted forecast as fallback
            ForecastResponse last = historyService.getLatestForecastForProduct(productId, months);
            if (last != null) {
                return last;
            }
            // If no persisted forecast, throw runtime
            throw new RuntimeException("Failed to fetch forecast and no cached forecast available", e);
        }
    }
}
