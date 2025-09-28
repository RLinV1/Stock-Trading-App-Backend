package com.example.demo.security;

import com.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JWTGenerator {

    private final AuthService authService;

    @Autowired
    public JWTGenerator(AuthService authService) {
        this.authService = authService;
    }
    public String generateToken(Authentication authentication) {
      return authService.generateToken(authentication);
    }

    // for refresh token
    public String generateToken(String username) {
        return authService.generateToken(username);
    }


    public String generateRefreshToken(Authentication authentication) {
        return authService.generateRefreshToken(authentication);
    }
    public String generateRefreshToken(String username) {
        return authService.generateRefreshToken(username);
    }

    public String getUsernameFromJwt(String token) {
       return authService.getUsernameFromJwt(token);
    }


    public boolean validateJwtToken(String authToken) {
        try {
            return authService.validateJwtToken(authToken);
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("Invalid JWT token: " + e.getMessage());
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            return authService.validateRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("Invalid refresh token: " + e.getMessage());
        }
    }





}
