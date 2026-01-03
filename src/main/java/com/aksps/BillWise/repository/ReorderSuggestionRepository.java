package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.ReorderSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReorderSuggestionRepository extends JpaRepository<ReorderSuggestion, Long> {
    List<ReorderSuggestion> findByProductId(Long productId);
    void deleteByProductIdAndForecastMonth(Long productId, LocalDate forecastMonth);
    List<ReorderSuggestion> findByStatus(String status);
}
