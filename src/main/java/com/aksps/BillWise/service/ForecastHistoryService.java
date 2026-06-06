package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.response.ForecastResponse;
import com.aksps.BillWise.model.ForecastHistory;
import com.aksps.BillWise.repository.ForecastHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ForecastHistoryService {

    private final ForecastHistoryRepository repo;
    private final ObjectMapper objectMapper;

    public ForecastHistoryService(ForecastHistoryRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    public void saveForecast(Long productId, int months, ForecastResponse response) {
        saveForecast(productId, months, response, null, null);
    }

    public void saveForecast(Long productId, int months, ForecastResponse response, String modelVersion, String frequency) {
        ForecastHistory history = new ForecastHistory();
        history.setProductId(productId);
        history.setMonths(months);
        history.setGeneratedAt(response.getGeneratedAt());
        history.setPredictedTotalRevenue(response.getPredictedTotalRevenue());
        history.setModelVersion(modelVersion);
        history.setFrequency(frequency);

        try {
            history.setDailyPredictionsJson(
                    objectMapper.writeValueAsString(response.getDailyPredictions())
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize prediction", e);
        }

        repo.save(history);
    }

    public ForecastResponse getLatestForecastForProduct(Long productId, int months) {
        List<ForecastHistory> list = repo.findByProductIdOrderByGeneratedAtDesc(productId);
        if (list == null || list.isEmpty()) {
            return null;
        }
        ForecastHistory latest = list.get(0);

        ForecastResponse resp = new ForecastResponse();
        resp.setProductId(latest.getProductId());
        resp.setGeneratedAt(latest.getGeneratedAt() != null ? latest.getGeneratedAt() : LocalDate.now());
        resp.setForecastingWindow(latest.getMonths() == null ? String.valueOf(months) : String.valueOf(latest.getMonths()));
        resp.setPredictedTotalRevenue(latest.getPredictedTotalRevenue());

        // deserialize dailyPredictionsJson into List<ForecastResponse.DailyPrediction>
        try {
            ForecastResponse.DailyPrediction[] arr = objectMapper.readValue(latest.getDailyPredictionsJson(), ForecastResponse.DailyPrediction[].class);
            List<ForecastResponse.DailyPrediction> preds = new ArrayList<>();
            if (arr != null) {
                for (ForecastResponse.DailyPrediction d : arr) {
                    preds.add(d);
                }
            }
            resp.setDailyPredictions(preds);
        } catch (Exception e) {
            // If deserialization fails, return minimal response without daily predictions
            resp.setDailyPredictions(new ArrayList<>());
        }

        return resp;
    }
}
