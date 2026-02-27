package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity                    // This class is an entity managed by Hibernate
@Table(name = "stock_transaction")  // This entity maps to the "stock_transaction" table in the DB
public class StockTransaction {
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

    private LocalDate date;       // Date of this stock data (no time)

    private double openPrice;     // Price at market open
    private double closePrice;    // Price at market close
    private double highPrice;     // Highest price during the day
    private double lowPrice;      // Lowest price during the day



    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    public StockTransaction(LocalDate date, double openPrice, double closePrice, double highPrice, double lowPrice, Stock stock) {
        this.date = date;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.stock = stock;
    }



}
