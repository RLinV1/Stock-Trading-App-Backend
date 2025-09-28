package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity                    // This class is an entity managed by Hibernate
@Table(name = "stocks")  // This entity maps to the "students" table in the DB
public class Stock {
    @SequenceGenerator(
            name = "stock_sequence",
            sequenceName = "stock_sequence",
            allocationSize = 1
    ) //Defines how to get IDs from the DB sequence
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "stock_sequence"
    ) //Tells Hibernate to generate IDs using that sequence generator
    @Id
    private Long id;
    private String symbol;

    private String name;

    private double currentPrice;

    public Stock(String symbol, String name, double currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
    }

}
