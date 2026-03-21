package com.example.demo.service;

import com.example.demo.models.Stock;
import com.example.demo.repository.StockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class StockPriceService {

    private final StockRepository stockRepository;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public StockPriceService(StockRepository stockRepository, ObjectMapper objectMapper) {
        this.stockRepository = stockRepository;
        this.objectMapper = objectMapper;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // Send current prices immediately on connect
        try {
            List<Stock> stocks = stockRepository.findAll();
            String json = objectMapper.writeValueAsString(stocks);
            emitter.send(SseEmitter.event().data(json));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Scheduled(fixedRate = 20000)
    public void scheduledPriceUpdate() {
        updateAllStockPrices();
    }

    public void updateAllStockPrices() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            double oldPrice = stock.getCurrentPrice();
            double newPrice = fluctuatePrice(oldPrice);
            stock.setCurrentPrice(newPrice);
        }

        stockRepository.saveAll(stocks);
        broadcast(stocks);
    }

    private void broadcast(List<Stock> stocks) {
        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                String json = objectMapper.writeValueAsString(stocks);
                emitter.send(SseEmitter.event().data(json));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
    }

    private double fluctuatePrice(double price) {
        double maxChangePercent = 0.05;
        double changePercent = (random.nextDouble() * 2 * maxChangePercent) - maxChangePercent;
        double newPrice = price + (price * changePercent);
        return Math.max(newPrice, 1);
    }
}
