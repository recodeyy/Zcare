package com.pharmacy.repository;

import com.pharmacy.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByNameContainingIgnoreCase(String name);

    List<Medicine> findByCategoryIgnoreCase(String category);

    // Find medicines expiring soon (within given days)
    @Query("SELECT m FROM Medicine m WHERE m.expiryDate <= :date ORDER BY m.expiryDate ASC")
    List<Medicine> findExpiringSoon(LocalDate date);

    // Find low stock medicines
    @Query("SELECT m FROM Medicine m WHERE m.stockQuantity <= :threshold ORDER BY m.stockQuantity ASC")
    List<Medicine> findLowStock(int threshold);

    boolean existsByBatchNumber(String batchNumber);
}
