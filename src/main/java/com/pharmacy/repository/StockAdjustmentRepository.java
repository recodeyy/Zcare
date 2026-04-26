package com.pharmacy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacy.model.StockAdjustment;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {

    Page<StockAdjustment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<StockAdjustment> findByMedicine_IdOrderByCreatedAtDesc(Long medicineId, Pageable pageable);
}