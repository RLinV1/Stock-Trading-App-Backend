package com.example.demo.controller;

import com.example.demo.dto.SellUserStockDto;
import com.example.demo.dto.UserStockDto;
import com.example.demo.exception.CustomErrorResponse;
import com.example.demo.models.UserStock;
import com.example.demo.service.UserStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// allow anyone to see other user stocks
@RestController
@RequestMapping(path="api/user-stock")
public class UserStockController {

    private final UserStockService userStockService;

    @Autowired
    public UserStockController(UserStockService userStockService){
        this.userStockService = userStockService;
    }

    @GetMapping
    public List<UserStockDto> getUserStocks(@RequestParam UUID userId){
        return userStockService.getUserStocks(userId).stream().map(userStock ->
                        new UserStockDto(
                                userStock.getId(),
                                userStock.getUser().getId(),
                                userStock.getStock().getId(),
                                userStock.getAvgCost(),
                                userStock.getShares()
                        )
                ).collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<UserStockDto> getAllUserStocks() {
        return userStockService.getAllUserStocks().stream().map(userStock ->
                new UserStockDto(
                        userStock.getId(),
                        userStock.getUser().getId(),
                        userStock.getStock().getId(),
                        userStock.getAvgCost(),
                        userStock.getShares()
                )
        ).collect(Collectors.toList());
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyUserStock(@RequestBody UserStockDto userStockDto) {

        try {
            UserStock userStock = userStockService.buyUserStock(userStockDto);

            return ResponseEntity.ok(new UserStockDto(
                    userStock.getId(),
                    userStock.getUser().getId(),
                    userStock.getStock().getId(),
                    userStock.getAvgCost(),
                    userStock.getShares()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new CustomErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellUserStock(@RequestBody SellUserStockDto sellUserStockDto){

        try {
            UserStock userStock = userStockService.sellUserStock(sellUserStockDto);

            return ResponseEntity.ok().body(new UserStockDto(
                    userStock.getId(),
                    userStock.getUser().getId(),
                    userStock.getStock().getId(),
                    userStock.getAvgCost(),
                    userStock.getShares()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(new CustomErrorResponse(e.getMessage()));
        }
    }


}
