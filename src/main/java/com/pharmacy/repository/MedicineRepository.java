package com.pharmacy.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacy.model.Medicine;

import jakarta.persistence.LockModeType;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByIsActiveTrue();

    Page<Medicine> findByIsActiveTrue(Pageable pageable);

    List<Medicine> findByIsActiveFalse();

    List<Medicine> findByStockQuantityLessThanAndIsActiveTrue(Integer quantity);

    List<Medicine> findByExpiryDateBeforeAndIsActiveTrue(LocalDate date);

    List<Medicine> findByExpiryDateBetweenAndIsActiveTrue(LocalDate startDate, LocalDate endDate);

    List<Medicine> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    List<Medicine> findByCategoryIgnoreCaseAndIsActiveTrue(String category);

    List<Medicine> findByBatchNumberIgnoreCaseAndIsActiveTrue(String batchNumber);

    Optional<Medicine> findByBarcodeAndIsActiveTrue(String barcode);

    Optional<Medicine> findByIdAndIsActiveTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medicine m where m.id = :id and m.isActive = true")
    Optional<Medicine> findActiveForUpdate(@Param("id") Long id);
}
