package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.Product;
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
     * Query to fetch top-selling products ranked by total revenue
     * based on InvoiceItem and Invoice data.
     */
    @Query("""
            SELECT p.id, p.name, p.sku, p.unitType, p.currentStock,
                   SUM(i.quantitySold * i.unitPriceAtSale) AS totalRevenue
            FROM InvoiceItem i
            JOIN i.product p
            JOIN i.invoice inv
            WHERE inv.invoiceDate >= :startDate
            GROUP BY p.id, p.name, p.sku, p.unitType, p.currentStock
            ORDER BY totalRevenue DESC
            """)
    List<Object[]> findTopSellingProductsByRevenue(@Param("startDate") LocalDateTime startDate);
}
