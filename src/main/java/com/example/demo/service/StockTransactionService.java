package com.example.demo.service;

import com.example.demo.models.StockTransaction;
import com.example.demo.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockTransactionService {

    private final StockTransactionRepository stockTransactionRepository;

    @Autowired
    public StockTransactionService(StockTransactionRepository stockTransactionRepository){
        this.stockTransactionRepository = stockTransactionRepository;
    }
    public List<StockTransaction> getAllStockTransactions(){
        return stockTransactionRepository.findAll();
    }

    public List<StockTransaction> getStockTransactionsByStockId(Long stockId){
        return stockTransactionRepository.findStockTransactionByStockId(stockId);

    }

}
