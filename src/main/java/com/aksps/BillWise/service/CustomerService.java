package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.CustomerRequest;
import com.aksps.BillWise.dto.response.CustomerResponse;
import com.aksps.BillWise.model.Customer;
import com.aksps.BillWise.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getContactNumber(),
                customer.getEmail(),
                customer.getGstNumber()
        );
    }

    /**
     * Fetches a customer by their contact number for automatic pre-filling at checkout.
     * This is the "extraordinary" lookup feature.
     */
    public Optional<CustomerResponse> getCustomerByContactNumber(String contactNumber) {
        return customerRepository.findByContactNumber(contactNumber)
                .map(this::mapToResponse);
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Simple check to prevent duplicate contact numbers on creation
        if (customerRepository.findByContactNumber(request.getContactNumber()).isPresent()) {
            throw new IllegalArgumentException("Customer with this contact number already exists.");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setContactNumber(request.getContactNumber());
        customer.setEmail(request.getEmail());
        customer.setGstNumber(request.getGstNumber());

        Customer savedCustomer = customerRepository.save(customer);
        return mapToResponse(savedCustomer);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
