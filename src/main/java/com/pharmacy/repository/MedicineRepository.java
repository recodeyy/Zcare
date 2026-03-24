package com.pharmacy.repository;

import com.pharmacy.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Page<Medicine> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Medicine> findByCategoryIgnoreCase(String category, Pageable pageable);

    // Find medicines expiring soon (within given days)
    @Query("SELECT m FROM Medicine m WHERE m.expiryDate <= :date")
    Page<Medicine> findExpiringSoon(LocalDate date, Pageable pageable);

    // Find low stock medicines
    @Query("SELECT m FROM Medicine m WHERE m.stockQuantity <= :threshold")
    Page<Medicine> findLowStock(int threshold, Pageable pageable);

    boolean existsByBatchNumber(String batchNumber);

    Optional<Medicine> findByBatchNumber(String batchNumber);
}
