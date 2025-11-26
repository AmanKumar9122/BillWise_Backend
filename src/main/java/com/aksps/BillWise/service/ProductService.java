package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.ProductRequest;
import com.aksps.BillWise.dto.response.ProductResponse;
import com.aksps.BillWise.model.Product;
import com.aksps.BillWise.model.UnitType;
import com.aksps.BillWise.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditService auditService;  // <<< ADDED
    private static final int DEFAULT_PAGE_SIZE = 20;

    public ProductService(ProductRepository productRepository, AuditService auditService) {
        this.productRepository = productRepository;
        this.auditService = auditService;
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

    // --------------------------------------------------------------
    //  PAGINATION + SEARCH (existing)
    // --------------------------------------------------------------
    public Page<ProductResponse> getProductsPaged(
            String search,
            Integer page,
            Integer size,
            String sortBy,
            String direction
    ) {
        int p = page != null ? Math.max(page, 0) : 0;
        int s = size != null ? Math.max(size, 1) : DEFAULT_PAGE_SIZE;

        Sort sort = Sort.by(
                direction != null && direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                sortBy != null ? sortBy : "id"
        );

        Pageable pageable = PageRequest.of(p, s, sort);

        Page<Product> result;

        if (search == null || search.isBlank()) {
            result = productRepository.findAll(pageable);
        } else {
            result = productRepository
                    .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(search, search, pageable);
        }

        return result.map(this::mapToResponse);
    }

    // --------------------------------------------------------------
    //  CREATE PRODUCT  (with Audit Logging)
    // --------------------------------------------------------------
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

        Product saved = productRepository.save(product);

        // AUDIT LOG — new product created
        auditService.logChange(saved.getId(), "PRODUCT_CREATED", "-", saved.getName());

        return mapToResponse(saved);
    }

    // --------------------------------------------------------------
    //  READ BY ID
    // --------------------------------------------------------------
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        return mapToResponse(product);
    }

    // --------------------------------------------------------------
    //  UPDATE PRODUCT  (with field-level Audit Logging)
    // --------------------------------------------------------------
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest req) {

        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        if (!p.getSku().equals(req.getSku()) &&
                productRepository.existsBySku(req.getSku())) {
            throw new IllegalArgumentException("SKU '" + req.getSku() + "' already exists.");
        }

        validateUnitType(req.getUnitType(), req.getBaseUnit());

        // --------- AUDIT CHANGES ---------
        if (!p.getName().equals(req.getName())) {
            auditService.logChange(id, "name", p.getName(), req.getName());
            p.setName(req.getName());
        }

        if (!p.getSku().equals(req.getSku())) {
            auditService.logChange(id, "sku", p.getSku(), req.getSku());
            p.setSku(req.getSku());
        }

        if (!p.getSellingPricePerBaseUnit().equals(req.getSellingPricePerBaseUnit())) {
            auditService.logChange(id, "sellingPricePerBaseUnit",
                    p.getSellingPricePerBaseUnit(),
                    req.getSellingPricePerBaseUnit());
            p.setSellingPricePerBaseUnit(req.getSellingPricePerBaseUnit());
        }

        if (!p.getUnitType().equals(req.getUnitType())) {
            auditService.logChange(id, "unitType", p.getUnitType(), req.getUnitType());
            p.setUnitType(req.getUnitType());
        }

        if (!p.getBaseUnit().equals(req.getBaseUnit())) {
            auditService.logChange(id, "baseUnit", p.getBaseUnit(), req.getBaseUnit());
            p.setBaseUnit(req.getBaseUnit());
        }

        if (!p.getCurrentStock().equals(req.getCurrentStock())) {
            auditService.logChange(id, "currentStock", p.getCurrentStock(), req.getCurrentStock());
            p.setCurrentStock(req.getCurrentStock());
        }

        if (!p.getMinStockLevel().equals(req.getMinStockLevel())) {
            auditService.logChange(id, "minStockLevel", p.getMinStockLevel(), req.getMinStockLevel());
            p.setMinStockLevel(req.getMinStockLevel());
        }

        // ----------------------------------

        return mapToResponse(productRepository.save(p));
    }

    // --------------------------------------------------------------
    //  READ ALL
    // --------------------------------------------------------------
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --------------------------------------------------------------
    //  DELETE PRODUCT (with audit)
    // --------------------------------------------------------------
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }

        auditService.logChange(id, "PRODUCT_DELETED", "-", "-");

        productRepository.deleteById(id);
    }

    // --------------------------------------------------------------
    //  HELPERS
    // --------------------------------------------------------------
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
