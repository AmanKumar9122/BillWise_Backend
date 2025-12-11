package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.ForecastHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForecastHistoryRepository extends JpaRepository<ForecastHistory, Long> {
    List<ForecastHistory> findByProductIdOrderByGeneratedAtDesc(Long productId);
}
