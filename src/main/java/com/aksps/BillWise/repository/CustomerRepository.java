package com.aksps.BillWise.repository;

import com.aksps.BillWise.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByContactNumber(String contactNumber);

    boolean existsByContactNumber(String contactNumber);

    Page<Customer> findByNameContainingIgnoreCaseOrContactNumberContainingIgnoreCase(
            String name, String contactNumber, Pageable pageable
    );
}
