package com.aksps.BillWise.service;

import com.aksps.BillWise.dto.request.CustomerRequest;
import com.aksps.BillWise.dto.response.CustomerResponse;
import com.aksps.BillWise.exception.ResourceNotFoundException;
import com.aksps.BillWise.exception.ValidationException;
import com.aksps.BillWise.model.Customer;
import com.aksps.BillWise.repository.CustomerRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private static final int DEFAULT_PAGE_SIZE = 20;

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
     * 🔍 PAGINATION + SEARCH
     */
    public Page<CustomerResponse> getPagedCustomers(
            String search,
            Integer page,
            Integer size
    ) {
        int p = page == null ? 0 : Math.max(page, 0);
        int s = size == null ? DEFAULT_PAGE_SIZE : Math.max(size, 1);

        Pageable pageable = PageRequest.of(p, s, Sort.by("name").ascending());

        Page<Customer> result;

        if (search == null || search.isBlank()) {
            result = customerRepository.findAll(pageable);
        } else {
            result = customerRepository.findByNameContainingIgnoreCaseOrContactNumberContainingIgnoreCase(
                    search, search, pageable
            );
        }

        return result.map(this::mapToResponse);
    }

    public Optional<CustomerResponse> getCustomerByContactNumber(String contactNumber) {
        return customerRepository.findByContactNumber(contactNumber).map(this::mapToResponse);
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {

        if (customerRepository.existsByContactNumber(request.getContactNumber())) {
            throw new ValidationException("Customer with this contact number already exists.");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setContactNumber(request.getContactNumber());
        customer.setEmail(request.getEmail());
        customer.setGstNumber(request.getGstNumber());

        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        // Prevent duplicate number
        if (!customer.getContactNumber().equals(request.getContactNumber()) &&
                customerRepository.existsByContactNumber(request.getContactNumber()))
        {
            throw new ValidationException("Another customer already uses this contact number.");
        }

        customer.setName(request.getName());
        customer.setContactNumber(request.getContactNumber());
        customer.setEmail(request.getEmail());
        customer.setGstNumber(request.getGstNumber());

        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Customer getOrCreateCustomer(String name, String contactNumber, String email, String gst) {

        return customerRepository.findByContactNumber(contactNumber)
                .orElseGet(() -> customerRepository.save(
                        new Customer(null, name, contactNumber, email, gst)
                ));
    }
}
