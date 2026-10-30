package com.example.rent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateEquipmentRequest(
        
        @NotBlank(message = "Name cannot be empty")
        String name,

        @NotBlank(message = "Category cannot be empty")
        String category,

        @NotNull(message = "Daily price is required")
        @Positive(message = "Daily price must be greater than zero")
        BigDecimal dailyPrice
) {}