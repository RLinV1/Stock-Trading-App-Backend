package com.example.demo.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SellUserStockDto {
    private Long id;
    private UUID userId;
    private Long stockId;
    private int shares;


    public SellUserStockDto(Long id, UUID userId, Long stockId, int shares) {
        this.id = id;
        this.userId = userId;
        this.stockId = stockId;
        this.shares = shares;
    }
}
