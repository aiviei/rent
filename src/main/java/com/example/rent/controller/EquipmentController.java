package com.example.rent.controller;

import com.example.rent.dto.CreateEquipmentRequest;
import com.example.rent.dto.EquipmentDto;
import com.example.rent.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    // GET /api/equipment
    @GetMapping
    public List<EquipmentDto> getAll() {
        return equipmentService.findAll();
    }

    // GET /api/equipment/{id}
    @GetMapping("/{id}")
    public EquipmentDto getById(@PathVariable Long id) {
        return equipmentService.findById(id);
    }

    // POST /api/equipment
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentDto create(@Valid @RequestBody CreateEquipmentRequest request) {
        return equipmentService.create(request);
    }

    // DELETE /api/equipment/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        equipmentService.delete(id);
    }
}