package com.example.demo.controller;

import com.example.demo.models.Stock;
import com.example.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="api/stock")
public class StockController {



    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService){
        this.stockService = stockService;
    }

    @GetMapping
    public List<Stock> getStocks(){
        return stockService.getStocks();
    }


    @PostMapping
    public ResponseEntity<?> addStock(@RequestBody Stock stock){
        try {
            Stock stockData= stockService.addNewStock(stock);
            return ResponseEntity.ok().body(stockData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public List<Stock> searchStocks(@RequestParam String query) {
        return stockService.searchStocks(query);
    }
    @GetMapping("/{id}")
    public Stock getStockById(@PathVariable Long id){
        return stockService.getStockById(id);
    }
    @DeleteMapping("/{id}")
    public void deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
    }
    @PutMapping("/{id}")
    public Stock updateStock(@PathVariable Long id,
                            @RequestParam(required = false) String symbol,
                            @RequestParam(required = false) String name,
                            @RequestParam(required = false) float currentPrice){
            return stockService.updateStock(id, symbol, name, currentPrice);


    }
}

