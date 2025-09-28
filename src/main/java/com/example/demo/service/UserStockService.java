package com.example.demo.service;

import com.example.demo.dto.SellUserStockDto;
import com.example.demo.dto.UserStockDto;
import com.example.demo.models.Stock;
import com.example.demo.models.UserEntity;
import com.example.demo.models.UserStock;
import com.example.demo.repository.StockRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserStockService {
    private final UserStockRepository userStockRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    @Autowired
    public UserStockService(UserStockRepository userStockRepository, UserRepository userRepository, StockRepository stockRepository){
        this.userStockRepository = userStockRepository;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
    }

    public List<UserStock> getAllUserStocks(){
        return userStockRepository.findAll();
    }

    public List<UserStock> getUserStocks(UUID userId){
        return userStockRepository.findByUserId(userId);
    }

    public BigDecimal getUserPortfolioValue(UUID userId){
        BigDecimal value = userStockRepository.getTotalPortfolioValue(userId);
        return value != null ? value : BigDecimal.ZERO;
    }

    // updates too if the stock already exists
    public UserStock buyUserStock(UserStockDto userStockDto){
        UserEntity user = userRepository.findById(userStockDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stock stock = stockRepository.findById(userStockDto.getStockId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));


        Optional<UserStock> optionalStock = userStockRepository.findByUserIdAndStockId(user.getId(), stock.getId());

        double totalCost = user.getCash() - userStockDto.getAvgCost() * userStockDto.getShares();

        if (totalCost <= 0) {
            throw new RuntimeException("You don't have enough cash");
        }

        user.setCash(user.getCash() - userStockDto.getAvgCost() * userStockDto.getShares());


        UserStock userStock;
        int newShares = userStockDto.getShares();
        double newAvgCost = userStockDto.getAvgCost();


        if (optionalStock.isPresent()){
            userStock = optionalStock.get();

            newShares = userStock.getShares() + userStockDto.getShares();
            newAvgCost = userStock.getAvgCost() * userStock.getShares() + userStockDto.getAvgCost() * userStockDto.getShares();
            newAvgCost = (newAvgCost / newShares);

            

            userStock.setShares(newShares);
            userStock.setAvgCost(newAvgCost);

        } else {
            userStock = new UserStock(user, stock, newAvgCost, newShares);

        }

        return userStockRepository.save(userStock);
    }

    public UserStock sellUserStock(SellUserStockDto sellUserStockDto){
        UserEntity user = userRepository.findById(sellUserStockDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Stock stock = stockRepository.findById(sellUserStockDto.getStockId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));
        Optional<UserStock> optionalStock = userStockRepository.findByUserIdAndStockId(user.getId(), stock.getId());


        if (optionalStock.isPresent()) {
            UserStock userStock = optionalStock.get();

            int newShares = userStock.getShares() - sellUserStockDto.getShares();

            if (newShares < 0) {
                throw new RuntimeException("You don't have enough shares");
            } else if (newShares > 0) {
                userStock.setShares(newShares);
                userStockRepository.save(userStock);
            } else {
                userStockRepository.delete(userStock);
            }
            user.setCash(user.getCash() + sellUserStockDto.getShares() * stock.getCurrentPrice());
            userRepository.save(user);

            return userStock;
        } else {
            throw new RuntimeException("Stock not found");
        }

    }
}
