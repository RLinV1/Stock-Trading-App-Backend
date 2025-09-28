package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_stock")
public class UserStock {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private int shares;
    private double avgCost;

    public UserStock(UserEntity user, Stock stock, double avgCost, int shares) {
        this.user = user;
        this.stock = stock;
        this.shares = shares;
        this.avgCost = avgCost;
    }
}
