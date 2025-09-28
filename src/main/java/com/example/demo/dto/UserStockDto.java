package com.example.demo.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserStockDto {
    private Long id;
    private UUID userId;
    private Long stockId;
    private double avgCost;
    private int shares;

    public UserStockDto(Long id, UUID userId, Long stockId, double avgCost, int shares) {
        this.id = id;
        this.userId = userId;
        this.stockId = stockId;
        this.avgCost = avgCost;
        this.shares = shares;
    }
}
