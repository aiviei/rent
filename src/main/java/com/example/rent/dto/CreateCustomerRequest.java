package com.example.rent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateCustomerRequest(
        
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Full name cannot be empty")
        String fullName,

        @NotBlank(message = "Phone cannot be empty")
        @Pattern(regexp = "^\\+?[0-9 ()\\-]{7,32}$", message = "Invalid phone format")
        String phone
) {}