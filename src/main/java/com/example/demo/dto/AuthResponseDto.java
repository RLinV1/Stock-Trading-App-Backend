package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer ";
    private String error;
    private String username;
    private List<String> roles;

    public AuthResponseDto(String accessToken, String refreshToken, String username, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.roles = roles;

    }
    public AuthResponseDto(String error) {
        this.error = error;
    }
}
