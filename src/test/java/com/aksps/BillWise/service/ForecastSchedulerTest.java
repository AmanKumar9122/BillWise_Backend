package com.aksps.BillWise.service;

import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ForecastSchedulerTest {

    @Test
    public void testRunDailyForecasts_pagesThroughProducts() {
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        ForecastService forecastService = Mockito.mock(ForecastService.class);

        Product p1 = new Product(); p1.setId(1L);
        Product p2 = new Product(); p2.setId(2L);

        Mockito.when(repo.findAll(PageRequest.of(0, 100))).thenReturn(new PageImpl<>(Arrays.asList(p1, p2)));
        Mockito.when(repo.findAll(PageRequest.of(1, 100))).thenReturn(new PageImpl<>(Arrays.asList()));

        ForecastScheduler scheduler = new ForecastScheduler(repo, forecastService, 100);
        scheduler.runDailyForecasts();

        // Verify forecastService.getForecast called for each product once
        verify(forecastService, times(1)).getForecast(1L, 1);
        verify(forecastService, times(1)).getForecast(2L, 1);
    }
}

