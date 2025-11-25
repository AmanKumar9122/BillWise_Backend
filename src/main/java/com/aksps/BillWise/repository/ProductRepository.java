package com.aksps.BillWise.repository;

import com.aksps.BillWise.dto.projection.TopProductProjection;
import com.aksps.BillWise.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);
    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.minStockLevel")
    List<Product> findProductsLowInStock();

    /**
     * Query to fetch top-selling products ranked by total revenue over a period.
     * Uses the new InvoiceItem entity and aggregates total revenue (lineTotal).
     *
     * Projection Order (critical for AnalyticsService mapping):
     * [0:id, 1:name, 2:sku, 3:unitType, 4:currentStock, 5:totalRevenue]
     */
    @Query("""
        SELECT i.product.id AS productId,
               i.product.name AS productName,
               i.product.sku AS sku,
               i.product.unitType AS unitType,
               i.product.currentStock AS currentStock,
               SUM(i.lineTotal) AS totalRevenue
        FROM InvoiceItem i
        WHERE i.invoice.invoiceDate >= :startDate
        GROUP BY i.product.id, i.product.name, i.product.sku, i.product.unitType, i.product.currentStock
        ORDER BY totalRevenue DESC
        """)
    Page<TopProductProjection> findTopSellingProductsByRevenue(
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable
    );

}