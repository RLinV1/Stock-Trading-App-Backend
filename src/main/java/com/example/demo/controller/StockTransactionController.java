package com.example.demo.controller;

import com.example.demo.dto.StockTransactionDto;
import com.example.demo.models.StockTransaction;
import com.example.demo.service.StockTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

//WIP (shows all stock history)
@RestController
@RequestMapping(path="api/stock-transaction")
public class StockTransactionController {

    private final StockTransactionService stockTransactionService;

    @Autowired
    public StockTransactionController(StockTransactionService stockTransactionService){
        this.stockTransactionService = stockTransactionService;
    }

    @GetMapping
    public List<StockTransaction> getAllStockTransactions(){
        return stockTransactionService.getAllStockTransactions();
    }

    @GetMapping(path="/{id}")
    public List<StockTransactionDto> getStockTransactionsByStockId(@PathVariable Long id){
        return stockTransactionService.getStockTransactionsByStockId(id)
                .stream().map(stockTransaction ->
                    new StockTransactionDto(
                            stockTransaction.getId(),
                            stockTransaction.getStock().getId(),
                            stockTransaction.getDate(),
                            stockTransaction.getOpenPrice(),
                            stockTransaction.getClosePrice(),
                            stockTransaction.getHighPrice(),
                            stockTransaction.getLowPrice()
                    )
                ).collect(Collectors.toList());

    }
}
