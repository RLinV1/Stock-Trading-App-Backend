package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PortfolioSnapshotDto {
    private Long id;
    private BigDecimal portfolioValue;
    private LocalDateTime dateTime;
    public PortfolioSnapshotDto(Long id, BigDecimal portfolioValue, LocalDateTime dateTime) {
        this.id = id;
        this.portfolioValue = portfolioValue;
        this.dateTime = dateTime;
    }

}
