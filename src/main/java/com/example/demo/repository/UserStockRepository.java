package com.example.demo.repository;

import com.example.demo.models.UserStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStockRepository extends JpaRepository<UserStock, Long> {
    Optional<UserStock> findByUserIdAndStockId(UUID userId, Long stockId);
    List<UserStock> findByUserId(UUID userId);
    @Query("SELECT SUM(us.shares * us.stock.currentPrice) FROM UserStock us JOIN us.stock s WHERE us.user.id = :userId")
    BigDecimal getTotalPortfolioValue(@Param("userId") UUID userId);


}
