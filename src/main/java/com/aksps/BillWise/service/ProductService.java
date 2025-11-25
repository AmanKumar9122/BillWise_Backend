package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.ProductRequest;
import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.model.UnitType;
import com.aksps.BillWise.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getSellingPricePerBaseUnit(),
                product.getUnitType(),
                product.getBaseUnit(),
                product.getCurrentStock()
        );
    }

    // --- CREATE ---
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU '" + request.getSku() + "' already exists.");
        }

        validateUnitType(request.getUnitType(), request.getBaseUnit());

        Product product = new Product(
                request.getName(),
                request.getSku(),
                request.getSellingPricePerBaseUnit(),
                request.getUnitType(),
                request.getBaseUnit(),
                request.getCurrentStock(),
                request.getMinStockLevel()
        );

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    // --- READ by ID ---
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        return mapToResponse(product);
    }

    // --- UPDATE ---
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        if (!product.getSku().equals(request.getSku()) &&
                productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU '" + request.getSku() + "' already exists.");
        }

        validateUnitType(request.getUnitType(), request.getBaseUnit());

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setSellingPricePerBaseUnit(request.getSellingPricePerBaseUnit());
        product.setUnitType(request.getUnitType());
        product.setBaseUnit(request.getBaseUnit());
        product.setCurrentStock(request.getCurrentStock());
        product.setMinStockLevel(request.getMinStockLevel());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    // --- READ ALL ---
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- DELETE ---
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    private void validateUnitType(UnitType type, String baseUnit) {
        String unit = baseUnit.toLowerCase().trim();

        if (type == UnitType.LIQUID && !(unit.contains("ml") || unit.contains("l"))) {
            throw new IllegalArgumentException("Base unit must reflect volume (ml/l) for LIQUID.");
        }
        if (type == UnitType.WEIGHT && !(unit.contains("g") || unit.contains("kg"))) {
            throw new IllegalArgumentException("Base unit must reflect mass (g/kg) for WEIGHT.");
        }
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
    }
}
