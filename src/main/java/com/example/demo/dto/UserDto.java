package com.example.demo.dto;

import java.util.List;
import java.util.UUID;

public record UserDto(String username, UUID userId, List<String> roleList, double cashBalance) {
}
