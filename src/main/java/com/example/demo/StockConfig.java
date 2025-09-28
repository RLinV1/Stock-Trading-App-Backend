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



            // stock is the basic data and stock item is the individual items and each date and stuff
           Stock stock = new Stock("VAL", "VALORANT", 120);
           Stock stock2 = new Stock("CS", "CounterStrike", 100);


            stockRepository.saveAll(List.of(stock, stock2));

            List<StockTransaction> items = new ArrayList<>();
            LocalDate start = LocalDate.of(2025, 7, 24);

            Random random = new Random();
            int startPrice = 100;

            for (int i = 0; i < 7; i++) {
                LocalDate date = start.plusDays(i);

                // generate bogus OHLC
                double open = startPrice + random.nextInt(10); // between 100 and 109
                double high = open + random.nextInt(5) + 1; // slightly above open
                double low  = open - random.nextInt(5) - 1; // slightly below open
                double close = low + (random.nextDouble() * (high - low)); // somewhere between low/high


                items.add(new StockTransaction(date, open, close, high, low, stock));

                open = startPrice + random.nextInt(10); // between 100 and 109
                high = open + random.nextInt(5) + 1; // slightly above open
                low  = open - random.nextInt(5) - 1; // slightly below open
                close = low + (random.nextDouble() * (high - low)); // somewhere between low/high

                items.add(new StockTransaction(date, open, close, high, low, stock2));

            }



            stockTransactionRepository.saveAll(items);

            UserStock userStock = new UserStock(user, stock, 120, 10 ); // 1200
            UserStock userStock2 = new UserStock(user, stock2, 130, 10); //

            userStockRepository.saveAll(List.of(userStock, userStock2));



        };
    }
}
