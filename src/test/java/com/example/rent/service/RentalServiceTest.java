package com.example.rent.service;

import com.example.rent.domain.*;
import com.example.rent.dto.CreateRentalRequest;
import com.example.rent.dto.RentalDto;
import com.example.rent.exception.BusinessRuleException;
import com.example.rent.repository.CustomerRepository;
import com.example.rent.repository.EquipmentRepository;
import com.example.rent.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private RentalService rentalService;

    private Customer customer;
    private Equipment equipment;
    private Rental rental;
    private CreateRentalRequest validRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);

        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setDailyPrice(new BigDecimal("100.00"));
        equipment.setStatus(EquipmentStatus.available);

        rental = new Rental();
        rental.setId(1L);
        rental.setStatus(RentalStatus.active);
        rental.setCustomer(customer);
        rental.setEquipment(equipment);

        validRequest = new CreateRentalRequest(1L, 1L, LocalDate.now(), LocalDate.now().plusDays(2));
    }

    @Test
    void createRental_ShouldThrowException_WhenEndDateBeforeStartDate() {
        CreateRentalRequest badRequest = new CreateRentalRequest(1L, 1L, LocalDate.now(), LocalDate.now().minusDays(1));
        
        assertThrows(BusinessRuleException.class, () -> rentalService.create(badRequest));
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void createRental_ShouldThrowException_WhenEquipmentNotAvailable() {
        equipment.setStatus(EquipmentStatus.maintenance);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        assertThrows(BusinessRuleException.class, () -> rentalService.create(validRequest));
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void createRental_ShouldThrowException_WhenDatesOverlap() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(rentalRepository.isEquipmentOverlapping(anyLong(), any(), any())).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> rentalService.create(validRequest));
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void createRental_ShouldCalculateCorrectPrice() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(rentalRepository.isEquipmentOverlapping(anyLong(), any(), any())).thenReturn(false);
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalDto result = rentalService.create(validRequest);

        assertEquals(new BigDecimal("123.00"), result.totalPrice());
        assertEquals(RentalStatus.active, result.status());
    }

    @Test
    void cancelRental_ShouldThrowException_WhenStatusCompleted() {
        rental.setStatus(RentalStatus.completed);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        assertThrows(BusinessRuleException.class, () -> rentalService.cancel(1L));
        verify(rentalRepository, never()).save(any());
    }
}