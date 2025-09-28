package com.example.demo.controller;

import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.LoginDto;
import com.example.demo.dto.RegisterDto;
import com.example.demo.dto.UserDto;
import com.example.demo.models.RefreshToken;
import com.example.demo.models.Role;
import com.example.demo.models.UserEntity;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JWTGenerator;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;

    @Autowired
    public AuthController(PasswordEncoder passwordEncoder, RoleRepository roleRepository,
                          UserRepository userRepository, AuthenticationManager authenticationManager,
                          JWTGenerator jwtGenerator, RefreshTokenRepository refreshTokenRepository,
                          AuthService authService) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authService = authService;
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto, BindingResult bindingResult) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }
        if (bindingResult.hasErrors()) {
            // Build a string with all error messages
            StringBuilder errors = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error -> {
                errors.append(error.getField())
                        .append(": ")
                        .append(error.getDefaultMessage())
                        .append("; ");
            });
            return ResponseEntity.badRequest().body(errors.toString());
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        Role roles = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Role USER not found"));
        user.setRoles(Collections.singleton(roles));
        user.setCash(10000);
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto){
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            List<String> roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String token = jwtGenerator.generateToken(authentication);
            String newRefreshToken;

            Optional<UserEntity> userOpt = userRepository.findByUsername(loginDto.getUsername());

            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();

                Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUser(user);

                existingRefreshToken.ifPresent(refreshTokenRepository::delete);

                 newRefreshToken = jwtGenerator.generateRefreshToken(user.getUsername());

            } else {
                newRefreshToken = jwtGenerator.generateRefreshToken(authentication);
            }

            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", token)
                    .httpOnly(true)       // prevent JS access
                    .secure(false)         // true if using HTTPS
                    .path("/")            // make accessible across the site
                    .maxAge(Duration.ofMinutes(60)) // short-lived
                    .sameSite("Strict")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true)
                    .secure(false) // true in production HTTPS
                    .path("/api/auth") // only send cookie on refresh endpoint
                    .maxAge(Duration.ofDays(7)) // adjust as needed
                        .sameSite("Strict")
                    .build();


            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(new AuthResponseDto(token, newRefreshToken, loginDto.getUsername(), roles));


        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto("Invalid username or password"));
        }
    }

    @PostMapping("refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // check cookie value
        String refreshToken = authService.getCookieValue(request, "refreshToken");

        System.out.println(refreshToken);

        if (refreshToken == null) {
            return ResponseEntity.status(403).body("Refresh token missing");
        }

        try {
            if (!jwtGenerator.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(403).body("Invalid refresh token");
            }

            String username = jwtGenerator.getUsernameFromJwt(refreshToken);
            String newAccessToken = jwtGenerator.generateToken(username);
            String newRefreshToken = jwtGenerator.generateRefreshToken(username);

            // Set new access token cookie
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                    .httpOnly(true)
                    .secure(false) // set true in production HTTPS
                    .path("/")
                    .maxAge(Duration.ofMinutes(60))
                    .sameSite("Strict")
                    .build();

            // Set new refresh token cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true)
                    .secure(false) // set true in production HTTPS
                    .path("/api/auth/refresh")
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Strict")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            // You can return a simple success message or empty body
            return ResponseEntity.ok().body("Tokens refreshed");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(403).body("Invalid refresh token");
        }
    }


    @GetMapping("/check")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("User is not authenticated!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String username = authentication.getName(); // username from the authenticated principal

        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            System.out.println("User is not authenticated!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        UserEntity user = userOpt.get();
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)  // assuming Role has a getName() method
                .toList();

        // Return user info DTO (avoid sensitive info like password)
        return ResponseEntity.ok(new UserDto(user.getUsername(), user.getId(), roleNames, user.getCash()));
    }

    @PostMapping("signout")
    public ResponseEntity<?> signOut(HttpServletResponse  response) {

        try {
            authService.clearAuthCookies(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error clearing auth cookies");
        }

        return ResponseEntity.ok("User signed out successfully");
    }


}
