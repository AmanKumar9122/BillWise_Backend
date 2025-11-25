// ProductManagementService.java (FIXED)
package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.ProductRequest;
import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.exception.ResourceNotFoundException;
import com.aksps.BillWise.exception.ValidationException;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductManagementService {

    private final ProductRepository productRepository;

    public ProductManagementService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

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

    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ValidationException("Product with SKU '" + request.getSku() + "' already exists.");
        }

        Product newProduct = new Product(
                request.getName(),
                request.getSku(),
                request.getSellingPricePerBaseUnit(),
                request.getUnitType(),
                request.getBaseUnit(),
                request.getCurrentStock(),
                request.getMinStockLevel()
        );

        Product savedProduct = productRepository.save(newProduct);
        return mapToProductResponse(savedProduct);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToProductResponse(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (!existingProduct.getSku().equals(request.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new ValidationException("Cannot update SKU: Already exists.");
        }

        existingProduct.setName(request.getName());
        existingProduct.setSku(request.getSku());
        existingProduct.setSellingPricePerBaseUnit(request.getSellingPricePerBaseUnit());
        existingProduct.setUnitType(request.getUnitType());
        existingProduct.setBaseUnit(request.getBaseUnit());
        existingProduct.setCurrentStock(request.getCurrentStock());
        existingProduct.setMinStockLevel(request.getMinStockLevel());

        Product updatedProduct = productRepository.save(existingProduct);
        return mapToProductResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }
}
