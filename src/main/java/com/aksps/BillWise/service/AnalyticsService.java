package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.ml.PredictionResponse;
import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.dto.response.TopProductResponse;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
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
     * Fetch products that have reached minimum stock levels.
     */
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findProductsLowInStock()
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calls ML Microservice asynchronously to predict demand.
     */
    public Mono<PredictionResponse> getPredictedSales(Long productId) {
        return mlIntegrationService.getSalesPrediction(productId);
    }

    /**
     * Retrieves top selling products ranked by revenue.
     *
     * @param days The evaluation period (ex: last 30 days)
     * @return List of TopProductResponse projection DTO
     */
    public List<TopProductResponse> getTopSellingProducts(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        List<Object[]> rawResults = productRepository.findTopSellingProductsByRevenue(startDate);

        return rawResults.stream()
                .map(record -> new TopProductResponse(
                        (Long) record[0],              // Product Id
                        (String) record[1],            // Product Name
                        (String) record[2],            // SKU
                        (String) record[3],            // Unit Type
                        (Integer) record[4],           // Current Stock
                        (BigDecimal) record[5]         // Revenue metric
                ))
                .collect(Collectors.toList());
    }

    /**
     * Helper mapper: Product Entity → ProductResponse DTO
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
