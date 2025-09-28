package com.example.demo.controller;

import com.example.demo.dto.PortfolioSnapshotDto;
import com.example.demo.models.PortfolioSnapshot;
import com.example.demo.service.PortfolioSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path="api/portfolio")
public class PortfolioSnapshotController {

    private final PortfolioSnapshotService portfolioSnapshotService;

    @Autowired
    public PortfolioSnapshotController(PortfolioSnapshotService portfolioSnapshotService){
        this.portfolioSnapshotService = portfolioSnapshotService;
    }


    @GetMapping("/{userId}")
    public ResponseEntity<List<PortfolioSnapshotDto>> getPortfolioSnapshot(@PathVariable UUID userId){
            List<PortfolioSnapshot> portfolioSnapshots = portfolioSnapshotService.getPortfolioSnapshotByUserId(userId);

            List<PortfolioSnapshotDto> portfolioSnapshotDtos = portfolioSnapshots.stream().map(snapshot ->
                    new PortfolioSnapshotDto(
                            snapshot.getId(),
                            snapshot.getPortfolioValue(),
                            snapshot.getDate()
                    )
            ).collect(Collectors.toList());
        return ResponseEntity.ok().body(portfolioSnapshotDtos);
    }
}
