package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.ProductRequest;
import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.model.UnitType; // Correct import assuming UnitType is nested
import com.aksps.BillWise.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * Service layer for all Product and Inventory management business logic.
 * Handles DTO conversion, validation, and transactional database operations.
 * Implements domain rules for unit types and SKU uniqueness.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Maps a Product entity to a simplified ProductResponse DTO for API output.
     */
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

    /**
     * Creates a new Product in the inventory.
     * @param request The DTO containing product details.
     * @return The created product as a response DTO.
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // Validation 1: Check if SKU already exists
        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product creation failed: SKU '" + request.getSku() + "' already exists.");
        }

        // Validation 2: Ensure unit type aligns with conventions (domain validation)
        validateUnitType(request.getUnitType(), request.getBaseUnit());

        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setSellingPricePerBaseUnit(request.getSellingPricePerBaseUnit());
        product.setUnitType(request.getUnitType());
        product.setBaseUnit(request.getBaseUnit()); // FIX: Save as String directly

        product.setCurrentStock(request.getCurrentStock());
        product.setMinStockLevel(request.getMinStockLevel());

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    /**
     * Retrieves a product by its ID.
     */
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        return mapToResponse(product);
    }

    /**
     * Updates an existing product's details.
     */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        // Validation: If SKU is changing, check for uniqueness against other products
        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product update failed: New SKU '" + request.getSku() + "' already exists.");
        }

        validateUnitType(request.getUnitType(), request.getBaseUnit());

        // Update fields from request DTO
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setSellingPricePerBaseUnit(request.getSellingPricePerBaseUnit());
        product.setUnitType(request.getUnitType());
        product.setBaseUnit(request.getBaseUnit()); // FIX: Save as String directly
        product.setCurrentStock(request.getCurrentStock());
        product.setMinStockLevel(request.getMinStockLevel());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    /**
     * Fetches all products in the inventory.
     */
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a product by ID.
     */
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Internal validation for unit type consistency.
     * Ensures that base unit strings are appropriate for the defined UnitType.
     */
    private void validateUnitType(UnitType type, String baseUnit) {
        String unit = baseUnit.toLowerCase().trim();

        // Enforce strong naming conventions for clear stock management
        if (type == UnitType.LIQUID && !(unit.contains("ml") || unit.contains("l"))) {
            throw new IllegalArgumentException("Base unit must reflect volume (e.g., 'ml' or 'l') for LIQUID type.");
        }
        if (type == UnitType.WEIGHT && !(unit.contains("g") || unit.contains("kg"))) {
            throw new IllegalArgumentException("Base unit must reflect mass (e.g., 'g' or 'kg') for WEIGHT type.");
        }
        if (type == UnitType.COUNT && !(unit.contains("pc") || unit.contains("box") || unit.contains("pack"))) {
            // Allows pieces, boxes, or packs, which are common for COUNT items.
        }
    }

    /**
     * Retrieves a Product entity by SKU. Used internally by the Billing Service.
     * @param sku The SKU of the product.
     * @return The Product entity.
     */
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
    }
}
