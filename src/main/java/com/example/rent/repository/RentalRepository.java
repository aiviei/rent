package com.example.rent.repository;

import com.example.rent.domain.Rental;
import com.example.rent.domain.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    
    boolean existsByCustomerIdAndStatus(Long customerId, RentalStatus status);
    
    boolean existsByEquipmentIdAndStatus(Long equipmentId, RentalStatus status);
    
    @Query("""
        SELECT COUNT(r) > 0 FROM Rental r 
        WHERE r.equipment.id = :equipmentId 
          AND r.status = 'active'
          AND r.startDate <= :endDate 
          AND r.endDate >= :startDate
    """)
    boolean isEquipmentOverlapping(
            @Param("equipmentId") Long equipmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}