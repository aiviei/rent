package com.example.rent.dto;

import java.time.Instant;

public record CustomerDto(
        Long id,
        String email,
        String fullName,
        String phone,
        Instant createdAt
) {}