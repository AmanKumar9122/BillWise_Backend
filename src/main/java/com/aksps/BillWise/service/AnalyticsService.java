package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.ml.PredictionResponse;
import com.aksps.BillWise.dto.projection.TopProductProjection;
import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.dto.response.TopProductResponse;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer responsible for analytics and insights.
 * Features:
 *  - Low stock alerts
 *  - ML-based sales forecasting
 *  - Top-selling products by revenue
 */
@Service
public class AnalyticsService {

    private final ProductRepository productRepository;
    private final MlIntegrationService mlIntegrationService;

    public AnalyticsService(ProductRepository productRepository,
                            MlIntegrationService mlIntegrationService) {
        this.productRepository = productRepository;
        this.mlIntegrationService = mlIntegrationService;
    }

    /**
     * Fetch products that are below minimum stock threshold.
     */
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findProductsLowInStock()
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calls ML microservice asynchronously to predict demand.
     */
    public Mono<PredictionResponse> getPredictedSales(Long productId) {
        return mlIntegrationService.getSalesPrediction(productId);
    }

    /**
     * Retrieves top selling products ranked by revenue in last N days.
     */
    public Page<TopProductResponse> getTopSellingProducts(int days, int page, int limit) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(page, limit);

        Page<TopProductProjection> resultPage =
                productRepository.findTopSellingProductsByRevenue(startDate, pageable);

        return resultPage.map(p -> new TopProductResponse(
                p.getProductId(),
                p.getProductName(),
                p.getSku(),
                p.getUnitType(),
                p.getCurrentStock(),
                p.getTotalRevenue()
        ));
    }


    /**
     * Helper: Product Entity → ProductResponse DTO Mapper
     */
    private ProductResponse mapToProductResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getSku(),
                p.getSellingPricePerBaseUnit(),
                p.getUnitType(),
                p.getBaseUnit(),
                p.getCurrentStock()
        );
    }
}
