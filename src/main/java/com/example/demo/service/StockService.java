package com.example.demo.service;

import com.example.demo.models.Stock;
import com.example.demo.repository.StockRepository;
import com.example.demo.repository.StockTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    @Autowired
    public StockService(StockRepository stockRepository, StockTransactionRepository stockTransactionRepository){
        this.stockRepository = stockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    public List<Stock> getStocks() {

        return stockRepository.findAll();
    }

    public Stock getStockById(Long id){
        return stockRepository.findById(id).orElseThrow(() -> new IllegalStateException("Stock with id " + id + " does not exist"));
    }
    public Stock addNewStock(Stock stock){


        stockRepository.findStockBySymbol(stock.getSymbol())
                .ifPresent(s -> {
                    throw new IllegalStateException("Stock with symbol " + stock.getSymbol() + " already exists");
                });
        return stockRepository.save(stock);
    }

    public void deleteStock(Long id){

        boolean exists = stockRepository.existsById(id);

        if (!exists){
            throw new IllegalStateException("Stock with id " + id + " does not exist");
        }
        stockTransactionRepository.deleteByStockId(id);
        stockRepository.deleteById(id);

    }

    @Transactional
    public Stock updateStock(Long id, String symbol, String name, double currentPrice){

        Stock stock = stockRepository.findById(id).orElseThrow(() -> new IllegalStateException("Stock with id " + id + " does not exist"));


        if (symbol != null && !symbol.equals(stock.getSymbol())) {
            stockRepository.findStockBySymbol(symbol)
                    .ifPresent(s -> {
                        throw new IllegalStateException("Stock with symbol " + stock.getSymbol() + " already exists");
                    });
            stock.setSymbol(symbol);
        }

        if (name != null && !name.equals(stock.getName())) {
            stock.setName(name);
        }

        if (currentPrice != stock.getCurrentPrice()) {
            stock.setCurrentPrice(currentPrice);
        }
        return stock;
    }


    public List<Stock> searchStocks(String query) {
        return stockRepository.findBySymbolStartingWithIgnoreCaseOrNameStartingWithIgnoreCase(query, query);
    }

}
