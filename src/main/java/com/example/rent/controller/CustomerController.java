package com.example.rent.controller;

import com.example.rent.dto.CreateCustomerRequest;
import com.example.rent.dto.CustomerDto;
import com.example.rent.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // GET /api/customers
    @GetMapping
    public List<CustomerDto> getAll() {
        return customerService.findAll();
    }

    // GET /api/customers/{id}
    @GetMapping("/{id}")
    public CustomerDto getById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    // POST /api/customers
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto create(@Valid @RequestBody CreateCustomerRequest request) {
        return customerService.create(request);
    }

    // DELETE /api/customers/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }
}