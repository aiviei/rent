package com.example.rent.controller;

import com.example.rent.dto.CreateRentalRequest;
import com.example.rent.dto.RentalDto;
import com.example.rent.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // GET /api/rentals
    @GetMapping
    public List<RentalDto> getAll() {
        return rentalService.findAll();
    }

    // GET /api/rentals/{id}
    @GetMapping("/{id}")
    public RentalDto getById(@PathVariable Long id) {
        return rentalService.findById(id);
    }

    // POST /api/rentals
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalDto create(@Valid @RequestBody CreateRentalRequest request) {
        return rentalService.create(request);
    }

    // PATCH /api/rentals/{id}/cancel
    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        rentalService.cancel(id);
    }
}