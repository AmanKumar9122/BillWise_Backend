package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.SalesData;
import com.aksps.BillWise.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Optional;

/**
 * Repository interface for managing aggregated SalesData entities.
 * Includes custom finders based on the composite unique key (Product + Month).
 */
@Repository
public interface SalesDataRepository extends JpaRepository<SalesData, Long> {

    /**
     * Finds a specific aggregated record based on the product entity and the YearMonth.
     * This method is critical for the DataAggregationScheduler to prevent duplicate entries
     * and update existing monthly sales records.
     *
     * @param product The Product entity.
     * @param month The aggregation period.
     * @return An Optional containing the SalesData record, if found.
     */
    Optional<SalesData> findByProductAndMonth(Product product, YearMonth month);
}