package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.ml.SalesDataPoint;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.model.SalesData;
import com.aksps.BillWise.repository.ProductRepository;
import com.aksps.BillWise.repository.SalesDataRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AISalesDataService {

    private final ProductRepository productRepository;
    private final SalesDataRepository salesDataRepository;

    public AISalesDataService(ProductRepository productRepository,
                              SalesDataRepository salesDataRepository) {
        this.productRepository = productRepository;
        this.salesDataRepository = salesDataRepository;
    }

    /**
     * Fetches monthly aggregated sales data for ML training.
     *
     * @param productId The product.
     * @return List of SalesDataPoint ("YYYY-MM", unitsSold)
     */
    public List<SalesDataPoint> getMonthlySales(Long productId) {

        // 1. Fetch product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        // 2. Fetch monthly aggregated data for that product
        List<SalesData> records = salesDataRepository.findByProductOrderByMonth(product);

        // 3. Convert to DTO list
        List<SalesDataPoint> result = new ArrayList<>();

        for (SalesData data : records) {
            SalesDataPoint point = new SalesDataPoint();

            // Convert YearMonth → "YYYY-MM"
            point.setDate(data.getMonth().toString());
            point.setUnitsSold(data.getTotalUnitsSold());

            result.add(point);
        }

        return result;
    }
}
