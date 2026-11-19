package com.example.rent.service;

import com.example.rent.domain.Customer;
import com.example.rent.domain.RentalStatus;
import com.example.rent.dto.CreateCustomerRequest;
import com.example.rent.dto.CustomerDto;
import com.example.rent.exception.BusinessRuleException;
import com.example.rent.exception.ResourceNotFoundException;
import com.example.rent.repository.CustomerRepository;
import com.example.rent.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final RentalRepository rentalRepository;

    public List<CustomerDto> findAll() {
        return customerRepository.findAll().stream().map(this::toDto).toList();
    }

    public CustomerDto findById(Long id) {
        return customerRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Transactional
    public CustomerDto create(CreateCustomerRequest req) {
        if (customerRepository.existsByEmail(req.email())) {
            throw new BusinessRuleException("Customer with email " + req.email() + " already exists");
        }

        Customer customer = new Customer();
        customer.setEmail(req.email());
        customer.setFullName(req.fullName());
        customer.setPhone(req.phone());

        return toDto(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        
        if (rentalRepository.existsByCustomerIdAndStatus(id, RentalStatus.active)) {
            throw new BusinessRuleException("Cannot delete customer with active rentals");
        }
        
        customerRepository.deleteById(id);
    }

    private CustomerDto toDto(Customer c) {
        return new CustomerDto(
                c.getId(), 
                c.getEmail(), 
                c.getFullName(), 
                c.getPhone(), 
                c.getCreatedAt()
        );
    }
}