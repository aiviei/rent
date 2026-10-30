package com.example.rent.dto;

import com.example.rent.domain.RentalStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RentalDto(
        Long id,
        Long customerId,
        Long equipmentId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalPrice,
        RentalStatus status
) {}