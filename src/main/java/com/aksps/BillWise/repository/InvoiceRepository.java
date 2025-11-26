package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("""
        SELECT i FROM Invoice i
        WHERE (:customerName IS NULL
               OR LOWER(i.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')))
          AND (:startDate IS NULL OR i.invoiceDate >= :startDate)
          AND (:endDate IS NULL OR i.invoiceDate <= :endDate)
        """)
    Page<Invoice> searchInvoices(
            @Param("customerName") String customerName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
