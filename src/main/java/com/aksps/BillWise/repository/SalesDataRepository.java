package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.SalesData;
import com.aksps.BillWise.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing aggregated SalesData entities.
 */
@Repository
public interface SalesDataRepository extends JpaRepository<SalesData, Long> {

    /**
     * Find SalesData by product and month (stored as LocalDate representing the first day of month).
     */
    Optional<SalesData> findByProductAndMonth(Product product, LocalDate month);

    List<SalesData> findByProductOrderByMonth(Product product);

}