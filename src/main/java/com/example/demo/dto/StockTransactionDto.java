package com.example.demo.dto;

import java.time.LocalDate;

public record StockTransactionDto(Long id, Long stockId,
                                  LocalDate date, double openPrice,
                                  double closePrice, double highPrice, double lowPrice ) {
}

