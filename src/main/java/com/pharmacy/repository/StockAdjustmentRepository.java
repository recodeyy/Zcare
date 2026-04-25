package com.pharmacy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacy.model.StockAdjustment;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {

    List<StockAdjustment> findAllByOrderByCreatedAtDesc();

    List<StockAdjustment> findByMedicine_IdOrderByCreatedAtDesc(Long medicineId);
}