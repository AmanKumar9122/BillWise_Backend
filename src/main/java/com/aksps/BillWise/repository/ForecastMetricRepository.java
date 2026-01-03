package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.ForecastMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForecastMetricRepository extends JpaRepository<ForecastMetric, Long> {
    List<ForecastMetric> findByProductId(Long productId);
}

