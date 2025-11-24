package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer responsible for analytics and insights.
 * Provides features like low stock alerts and ML-based demand forecasting.
 */
@Service
public class AnalyticsService {

    private final ProductRepository productRepository;
    private final MlIntegrationService mlIntegrationService;  // ML microservice calling bridge

    public AnalyticsService(ProductRepository productRepository, MlIntegrationService mlIntegrationService) {
        this.productRepository = productRepository;
        this.mlIntegrationService = mlIntegrationService;
    }

    /**
     * Returns list of products whose stock is below minimum alert threshold.
     * Converts Product entity to ProductResponse DTO for clean output.
     */
    public List<ProductResponse> getLowStockProducts() {
        List<Product> lowStockProducts = productRepository.findProductsLowInStock();

        return lowStockProducts.stream()
                .map(p -> new ProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getSku(),
                        p.getSellingPricePerBaseUnit(),
                        p.getUnitType(),
                        p.getBaseUnit(),
                        p.getCurrentStock()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Calls ML Forecasting Microservice asynchronously using WebClient.
     * Returns Mono<Object> to support reactive non-blocking processing.
     */
    public Mono<Object> getPredictedSales(Long productId) {
        // Delegates prediction logic to ML Integration bridge
        return mlIntegrationService.getSalesPrediction(productId).cast(Object.class);
    }
}
