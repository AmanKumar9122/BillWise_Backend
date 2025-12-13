package com.aksps.BillWise.service;

import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Component
public class ForecastScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ForecastScheduler.class);
    private final ProductRepository productRepository;
    private final ForecastService forecastService;

    private final int pageSize;

    public ForecastScheduler(ProductRepository productRepository,
                              ForecastService forecastService,
                              @Value("${ml.batch.page-size:100}") int pageSize) {
        this.productRepository = productRepository;
        this.forecastService = forecastService;
        this.pageSize = pageSize;
    }

    // Cron expression configurable via application.properties (default: nightly at 02:00)
    @Scheduled(cron = "${ml.batch.cron:0 0 2 * * ?}")
    public void runDailyForecasts() {
        logger.info("Starting scheduled batch forecast job");
        int page = 0;
        while (true) {
            List<Product> products = productRepository.findAll(PageRequest.of(page, pageSize)).getContent();
            if (products.isEmpty()) break;
            for (Product p : products) {
                try {
                    forecastService.getForecast(p.getId(), 1); // predict 1 month ahead
                } catch (Exception e) {
                    logger.error("Failed to fetch forecast for product {}: {}", p.getId(), e.getMessage());
                }
            }
            page++;
        }
        logger.info("Finished scheduled batch forecast job");
    }
}
