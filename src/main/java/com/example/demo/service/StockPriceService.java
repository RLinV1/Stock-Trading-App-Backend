package com.example.demo.service;

import com.example.demo.models.Stock;
import com.example.demo.repository.StockRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class StockPriceService {

    private final StockRepository stockRepository;

    private final Random random = new Random();

    public StockPriceService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void updateAllStockPrices() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            double oldPrice = stock.getCurrentPrice();  // assuming you have this field
            double newPrice = fluctuatePrice(oldPrice);
            stock.setCurrentPrice(newPrice);
        }

        stockRepository.saveAll(stocks);
    }

    private double fluctuatePrice(double price) {
        double maxChangePercent = 0.05; // 5% up or down
        double changePercent = (random.nextDouble() * 2 * maxChangePercent) - maxChangePercent;
        double newPrice = price + (price * changePercent);
        return Math.max(newPrice, 1); // don't go below 1
    }

    @Scheduled(fixedRate = 20000) // runs every 20 seconds
    public void scheduledPriceUpdate() {
        updateAllStockPrices();
//        System.out.println("Updated stock prices at " + LocalDateTime.now());
    }
}

