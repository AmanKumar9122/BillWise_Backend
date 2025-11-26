package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.ProductAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductAuditLogRepository extends JpaRepository<ProductAuditLog, Long> {
    List<ProductAuditLog> findByProductIdOrderByTimestampDesc(Long productId);
}
