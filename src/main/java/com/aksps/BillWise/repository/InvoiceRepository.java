package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access layer for the Invoice entity.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // Extra query methods (filter by date, customer, etc.) can be added later
}
