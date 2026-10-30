package com.example.rent.service;

import com.example.rent.domain.*;
import com.example.rent.dto.CreateRentalRequest;
import com.example.rent.dto.RentalDto;
import com.example.rent.exception.BusinessRuleException;
import com.example.rent.exception.ResourceNotFoundException;
import com.example.rent.repository.CustomerRepository;
import com.example.rent.repository.EquipmentRepository;
import com.example.rent.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalService {
    private final RentalRepository rentalRepository;
    private final CustomerRepository customerRepository;
    private final EquipmentRepository equipmentRepository;

    public List<RentalDto> findAll() {
        return rentalRepository.findAll().stream().map(this::toDto).toList();
    }

    public RentalDto findById(Long id) {
        return rentalRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with id: " + id));
    }

    @Transactional
    public RentalDto create(CreateRentalRequest req) {
        // Бизнес-правило 1: дата окончания должна быть позже даты начала
        if (!req.endDate().isAfter(req.startDate())) {
            throw new BusinessRuleException("End date must be after start date");
        }

        // Ищем клиента и оборудование
        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + req.customerId()));
        
        Equipment equipment = equipmentRepository.findById(req.equipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + req.equipmentId()));

        // Бизнес-правило 2: оборудование должно быть доступно (не в ремонте и не списано)
        if (equipment.getStatus() != EquipmentStatus.available) {
            throw new BusinessRuleException("Equipment is not available for rent. Current status: " + equipment.getStatus());
        }

        // Бизнес-правило 3: проверка пересечения дат (через репозиторий)
        if (rentalRepository.isEquipmentOverlapping(equipment.getId(), req.startDate(), req.endDate())) {
            throw new BusinessRuleException("Equipment is already booked for selected dates");
        }

        // Бизнес-правило 4: Расчет стоимости (включая первый и последний день)
        long days = ChronoUnit.DAYS.between(req.startDate(), req.endDate()) + 1;
        BigDecimal totalPrice = equipment.getDailyPrice().multiply(BigDecimal.valueOf(days));

        Rental rental = new Rental();
        rental.setCustomer(customer);
        rental.setEquipment(equipment);
        rental.setStartDate(req.startDate());
        rental.setEndDate(req.endDate());
        rental.setTotalPrice(totalPrice);
        rental.setStatus(RentalStatus.active);

        return toDto(rentalRepository.save(rental));
    }

    @Transactional
    public void cancel(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with id: " + id));

        // Бизнес-правило 5: нельзя отменить уже завершенную аренду
        if (rental.getStatus() == RentalStatus.completed) {
            throw new BusinessRuleException("Cannot cancel a completed rental");
        }
        
        // Бизнес-правило 6: нельзя отменить уже отмененную аренду
        if (rental.getStatus() == RentalStatus.cancelled) {
            throw new BusinessRuleException("Rental is already cancelled");
        }

        rental.setStatus(RentalStatus.cancelled);
        rentalRepository.save(rental);
    }

    private RentalDto toDto(Rental r) {
        return new RentalDto(
                r.getId(), 
                r.getCustomer().getId(), 
                r.getEquipment().getId(), 
                r.getStartDate(), 
                r.getEndDate(), 
                r.getTotalPrice(), 
                r.getStatus()
        );
    }
}