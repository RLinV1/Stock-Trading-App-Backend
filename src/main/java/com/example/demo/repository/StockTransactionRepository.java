package com.example.demo.repository;

import com.example.demo.models.StockTransaction;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findStockTransactionByStockId(Long stockId);

    @Modifying
    @Transactional
    @Query("DELETE FROM StockTransaction s WHERE s.stock.id = ?1")
    void deleteByStockId(Long stockId);



}
