package com.example.demo.repository;

import com.example.demo.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    @Query("SELECT s FROM Stock s WHERE s.symbol = ?1")
    Optional<Stock> findStockBySymbol(String symbol);


    List<Stock> findByNameContainingIgnoreCase(String name);

    List<Stock> findBySymbolStartingWithIgnoreCaseOrNameStartingWithIgnoreCase(String symbol, String name);


}
