package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.ForecastResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ForecastResultRepository extends JpaRepository<ForecastResult, Long> {

    void deleteByProductIdAndForecastMonth(Long productId, LocalDate forecastMonth);

    List<ForecastResult> findByProductId(Long productId);
}

