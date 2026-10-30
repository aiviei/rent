package com.example.rent.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateRentalRequest(
        
        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotNull(message = "Equipment ID is required")
        Long equipmentId,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate
) {}