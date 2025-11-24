package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);
    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.minStockLevel")
    List<Product> findProductsLowInStock();
}
