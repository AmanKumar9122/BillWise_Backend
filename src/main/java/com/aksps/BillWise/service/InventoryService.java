package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.StockAdjustmentRequest;
import com.aksps.BillWise.exception.ResourceNotFoundException;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.model.StockAdjustment;
import com.aksps.BillWise.repository.ProductRepository;
import com.aksps.BillWise.repository.StockAdjustmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockAdjustmentRepository adjustmentRepository;

    public InventoryService(ProductRepository productRepository,
                            StockAdjustmentRepository adjustmentRepository) {
        this.productRepository = productRepository;
        this.adjustmentRepository = adjustmentRepository;
    }

    @Transactional
    public void adjustStock(StockAdjustmentRequest request) {

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));

        int newStock = product.getCurrentStock() + request.quantityChange();
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }

        // Update stock
        product.setCurrentStock(newStock);
        productRepository.save(product);

        // Extract user from JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";

        // Save adjustment history
        StockAdjustment adj = new StockAdjustment();
        adj.setProductId(product.getId());
        adj.setQuantityChange(request.quantityChange());
        adj.setReason(request.reason());
        adj.setAdjustedBy(username);

        adjustmentRepository.save(adj);
    }

    public List<StockAdjustment> getAllAdjustments() {
        return adjustmentRepository.findAll();
    }
}
