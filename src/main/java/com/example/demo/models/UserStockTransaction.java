package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_stock_transaction")
@Data
public class UserStockTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserEntity user;

    @ManyToOne(optional = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private int shares;
    private double price;

    private LocalDateTime timestamp;
}
