package com.example.demo.service;

import com.example.demo.models.PortfolioSnapshot;
import com.example.demo.models.UserEntity;
import com.example.demo.repository.PortfolioSnapshotRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioSnapshotService {

    private final UserRepository userRepository;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final UserStockService userStockService;

    @Autowired
    public PortfolioSnapshotService(UserRepository userRepository,
                                    PortfolioSnapshotRepository snapshotRepository,
                                    UserStockService userStockService) {
        this.userRepository = userRepository;
        this.snapshotRepository = snapshotRepository;
        this.userStockService = userStockService;
    }

//    @Scheduled(cron = "0 0 17 * * *") //
    @Scheduled(fixedRate = 20000)
    public void createDailySnapshots() {
        List<UserEntity> users = userRepository.findAll();
        LocalDateTime today = LocalDateTime.now().withNano(0);

        for (UserEntity user : users) {
            BigDecimal value = userStockService.getUserPortfolioValue(user.getId());

            PortfolioSnapshot snapshot = new PortfolioSnapshot();
            snapshot.setUser(user);
            snapshot.setDate(today);
            snapshot.setPortfolioValue(value);

            snapshotRepository.save(snapshot);
        }
    }

    public  List<PortfolioSnapshot> getPortfolioSnapshotByUserId(UUID userId){
        return snapshotRepository.findByUserIdOrderByDateAsc(userId);
    }


}
