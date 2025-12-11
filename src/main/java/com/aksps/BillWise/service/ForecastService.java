package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.response.ForecastResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ForecastService {

    private final WebClient webClient;
    private final ForecastHistoryService historyService;

    public ForecastService(WebClient.Builder builder,
                           @Value("${ml.service.base-url:http://localhost:5000}") String baseUrl,
                           ForecastHistoryService historyService) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.historyService = historyService;
    }

    public ForecastResponse getForecast(Long productId, int months) {

        String url = "/forecast?product_id=" + productId + "&months=" + months;

        ForecastResponse response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(ForecastResponse.class)
                .block();

        // Save forecast to DB
        historyService.saveForecast(productId, months, response);

        return response;
    }
}

