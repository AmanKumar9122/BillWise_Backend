package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.response.ForecastResponse;
import com.aksps.BillWise.model.ForecastHistory;
import com.aksps.BillWise.repository.ForecastHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ForecastHistoryService {

    private final ForecastHistoryRepository repo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ForecastHistoryService(ForecastHistoryRepository repo) {
        this.repo = repo;
    }

    public void saveForecast(Long productId, int months, ForecastResponse response) {
        ForecastHistory history = new ForecastHistory();
        history.setProductId(productId);
        history.setMonths(months);
        history.setGeneratedAt(response.getGeneratedAt());
        history.setPredictedTotalRevenue(response.getPredictedTotalRevenue());

        try {
            history.setDailyPredictionsJson(
                    objectMapper.writeValueAsString(response.getDailyPredictions())
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize prediction", e);
        }

        repo.save(history);
    }
}
