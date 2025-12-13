package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.ReorderOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReorderOverrideRepository extends JpaRepository<ReorderOverride, Long> {
    List<ReorderOverride> findByProductIdOrderByCreatedAtDesc(Long productId);
}

