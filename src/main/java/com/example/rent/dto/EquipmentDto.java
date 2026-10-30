package com.example.rent.dto;

import com.example.rent.domain.EquipmentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record EquipmentDto(
        Long id,
        String name,
        String category,
        BigDecimal dailyPrice,
        EquipmentStatus status,
        Instant createdAt
) {}