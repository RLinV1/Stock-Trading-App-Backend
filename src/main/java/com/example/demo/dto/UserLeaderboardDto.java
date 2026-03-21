package com.example.demo.dto;

/**
 * Exposes only the username and cash balance for the leaderboard.
 * Avoids leaking sensitive user data (id, roles, password) to the client.
 */
public record UserLeaderboardDto(String username, double cashBalance) {
}
