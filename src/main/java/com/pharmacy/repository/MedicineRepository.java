package com.pharmacy.repository;

import com.pharmacy.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByStockQuantityLessThan(Integer quantity);

    List<Medicine> findByExpiryDateBefore(LocalDate date);

    List<Medicine> findByNameContainingIgnoreCase(String name);
}
