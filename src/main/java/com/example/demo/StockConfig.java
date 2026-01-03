package com.example.demo;

import com.example.demo.models.*;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@Configuration
public class StockConfig {

    @Bean
    CommandLineRunner commandLineRunner(StockRepository stockRepository,
                                        StockTransactionRepository stockTransactionRepository,
                                        RoleRepository roleRepository,
                                        UserRepository userRepository,
                                        UserStockRepository userStockRepository){
        return args -> {

            PasswordEncoder encoder = new BCryptPasswordEncoder();

            Role role = new Role("ADMIN");
            Role role1 = new Role("USER");
            roleRepository.saveAll(List.of(role, role1));

            UserEntity user = new UserEntity();
            user.setUsername("user");
            user.setPassword(encoder.encode("password1"));
            user.setCash(10000);
            user.setRoles(new HashSet<>(List.of(role)));

            UserEntity user2 = new UserEntity();
            user2.setUsername("user2");
            user2.setPassword(encoder.encode("password1"));
            user2.setCash(10000);
            user2.setRoles(new HashSet<>(List.of(role)));

            userRepository.saveAll(List.of(user, user2));

            // Create 50 random stocks
            List<Stock> stocks = List.of(
                    new Stock("AAPL", "Apple Inc.", 175.50),
                    new Stock("GOOGL", "Alphabet Inc.", 142.30),
                    new Stock("MSFT", "Microsoft Corporation", 378.90),
                    new Stock("AMZN", "Amazon.com Inc.", 178.25),
                    new Stock("TSLA", "Tesla Inc.", 248.50),
                    new Stock("META", "Meta Platforms Inc.", 485.75),
                    new Stock("NVDA", "NVIDIA Corporation", 875.28),
                    new Stock("NFLX", "Netflix Inc.", 612.45),
                    new Stock("DIS", "Walt Disney Company", 112.80),
                    new Stock("PYPL", "PayPal Holdings Inc.", 78.90),
                    new Stock("ADBE", "Adobe Inc.", 598.33),
                    new Stock("INTC", "Intel Corporation", 43.65),
                    new Stock("CSCO", "Cisco Systems Inc.", 56.42),
                    new Stock("CMCSA", "Comcast Corporation", 42.18),
                    new Stock("PEP", "PepsiCo Inc.", 171.25),
                    new Stock("COST", "Costco Wholesale", 789.60),
                    new Stock("AVGO", "Broadcom Inc.", 1345.78),
                    new Stock("TXN", "Texas Instruments", 189.42),
                    new Stock("QCOM", "Qualcomm Inc.", 178.90),
                    new Stock("TMUS", "T-Mobile US Inc.", 165.33),
                    new Stock("AMGN", "Amgen Inc.", 287.65),
                    new Stock("HON", "Honeywell International", 208.44),
                    new Stock("SBUX", "Starbucks Corporation", 98.75),
                    new Stock("INTU", "Intuit Inc.", 645.22),
                    new Stock("AMD", "Advanced Micro Devices", 165.88),
                    new Stock("GILD", "Gilead Sciences Inc.", 82.40),
                    new Stock("AMAT", "Applied Materials Inc.", 198.55),
                    new Stock("BKNG", "Booking Holdings Inc.", 3567.89),
                    new Stock("MDLZ", "Mondelez International", 72.18),
                    new Stock("ADP", "Automatic Data Processing", 268.44),
                    new Stock("ISRG", "Intuitive Surgical Inc.", 445.67),
                    new Stock("REGN", "Regeneron Pharmaceuticals", 987.23),
                    new Stock("VRTX", "Vertex Pharmaceuticals", 412.90),
                    new Stock("LRCX", "Lam Research Corporation", 878.45),
                    new Stock("PANW", "Palo Alto Networks", 325.60),
                    new Stock("SNPS", "Synopsys Inc.", 567.33),
                    new Stock("CDNS", "Cadence Design Systems", 298.77),
                    new Stock("KLAC", "KLA Corporation", 678.90),
                    new Stock("MRVL", "Marvell Technology", 78.45),
                    new Stock("CRWD", "CrowdStrike Holdings", 298.50),
                    new Stock("ADSK", "Autodesk Inc.", 256.78),
                    new Stock("ABNB", "Airbnb Inc.", 145.67),
                    new Stock("WDAY", "Workday Inc.", 234.89),
                    new Stock("TEAM", "Atlassian Corporation", 198.44),
                    new Stock("DXCM", "DexCom Inc.", 87.65),
                    new Stock("MCHP", "Microchip Technology", 89.23),
                    new Stock("FTNT", "Fortinet Inc.", 67.88),
                    new Stock("NXPI", "NXP Semiconductors", 234.56),
                    new Stock("PAYX", "Paychex Inc.", 128.90),
                    new Stock("MNST", "Monster Beverage", 56.34)
            );

            stockRepository.saveAll(stocks);

            // Generate stock transactions for all stocks
            List<StockTransaction> items = new ArrayList<>();
            LocalDate start = LocalDate.of(2025, 7, 24);
            Random random = new Random();

            for (Stock stock : stocks) {
                double basePrice = stock.getCurrentPrice();

                for (int i = 0; i < 7; i++) {
                    LocalDate date = start.plusDays(i);

                    // Generate realistic OHLC data within ±5% of current price
                    double variance = basePrice * 0.05; // 5% variance
                    double open = basePrice + (random.nextDouble() * variance * 2) - variance;
                    double high = open + (random.nextDouble() * variance);
                    double low = open - (random.nextDouble() * variance);
                    double close = low + (random.nextDouble() * (high - low));

                    items.add(new StockTransaction(date, open, close, high, low, stock));
                }
            }

            stockTransactionRepository.saveAll(items);

            // Create some user stocks for the first user with random stocks
            UserStock userStock1 = new UserStock(user, stocks.get(0), 175.50, 10);  // AAPL
            UserStock userStock2 = new UserStock(user, stocks.get(4), 248.50, 5);   // TSLA
            UserStock userStock3 = new UserStock(user, stocks.get(6), 875.28, 2);   // NVDA

            userStockRepository.saveAll(List.of(userStock1, userStock2, userStock3));
        };
    }
}
