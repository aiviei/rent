package com.example.rent.service;

import com.example.rent.domain.Equipment;
import com.example.rent.domain.EquipmentStatus;
import com.example.rent.domain.RentalStatus;
import com.example.rent.dto.CreateEquipmentRequest;
import com.example.rent.dto.EquipmentDto;
import com.example.rent.exception.BusinessRuleException;
import com.example.rent.exception.ResourceNotFoundException;
import com.example.rent.repository.EquipmentRepository;
import com.example.rent.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final RentalRepository rentalRepository;

    public List<EquipmentDto> findAll() {
        return equipmentRepository.findAll().stream().map(this::toDto).toList();
    }

    public EquipmentDto findById(Long id) {
        return equipmentRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
    }

    @Transactional
    public EquipmentDto create(CreateEquipmentRequest req) {
        Equipment eq = new Equipment();
        eq.setName(req.name());
        eq.setCategory(req.category());
        eq.setDailyPrice(req.dailyPrice());
        eq.setStatus(EquipmentStatus.available); // дефолтный статус

        return toDto(equipmentRepository.save(eq));
    }

    @Transactional
    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipment not found with id: " + id);
        }

        // Бизнес-правило: нельзя удалить оборудование, которое в аренде
        if (rentalRepository.existsByEquipmentIdAndStatus(id, RentalStatus.active)) {
            throw new BusinessRuleException("Cannot delete equipment that is currently rented");
        }

        equipmentRepository.deleteById(id);
    }

    private EquipmentDto toDto(Equipment e) {
        return new EquipmentDto(
                e.getId(),
                e.getName(),
                e.getCategory(),
                e.getDailyPrice(),
                e.getStatus(),
                e.getCreatedAt()
        );
    }
}