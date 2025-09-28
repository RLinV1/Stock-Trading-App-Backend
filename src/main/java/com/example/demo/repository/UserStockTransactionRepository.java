package com.example.demo.repository;

import com.example.demo.models.UserStockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStockTransactionRepository extends JpaRepository<UserStockTransaction, Long> {
    Optional<UserStockTransaction> findUserStockTransactionById(Long stockTransactionId);

}
