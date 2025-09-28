package com.example.demo.service;

import com.example.demo.exception.TokenRefreshException;
import com.example.demo.models.RefreshToken;
import com.example.demo.models.UserEntity;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class AuthService {
    private static final long JWT_EXPIRATION = 60 * 60 * 1000;
    @Value("${jwt.secret}")
    private String JWT_SECRET;
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days


    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuthService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .subject(username) // new method replaces setSubject()
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key) // no algorithm needed, inferred from key
                .compact();

    }

    // for refresh token
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .subject(username)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }


    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jti = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(REFRESH_TOKEN_VALIDITY);

        String jwt =  Jwts.builder()
                .subject(authentication.getName())
                .id(jti) // jti claim
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(Date.from(expiryDate))
                .signWith(key)
                .compact();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jti); // store only the jti
        refreshToken.setExpiryDate(expiryDate);
        refreshTokenRepository.save(refreshToken);


        return jwt;


    }

    @Transactional
    public String generateRefreshToken(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jti = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(REFRESH_TOKEN_VALIDITY);


        String newJwt = Jwts.builder()
                .subject(username)
                .id(jti) // jti claim
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(Date.from(expiryDate))
                .signWith(key)
                .compact();

        RefreshToken stored = refreshTokenRepository.findByUser(user)
                .map(existing -> {
                    existing.setToken(jti);
                    existing.setExpiryDate(expiryDate);
                    return existing;
                })
                .orElseGet(() -> {
                    RefreshToken token = new RefreshToken();
                    token.setUser(user);
                    token.setToken(jti);
                    token.setExpiryDate(expiryDate);
                    return token;
                });

        refreshTokenRepository.save(stored);


        return newJwt;
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }


    public boolean validateJwtToken(String authToken) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(authToken)
                .getPayload();
        if (!"access".equals(claims.get("type", String.class))) {
            // Reject the token (throw exception or ignore authentication)
            throw new AuthenticationCredentialsNotFoundException("Invalid token type for access");
        }
        return true;

    }
    public boolean validateRefreshToken(String refreshToken) {

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new AuthenticationCredentialsNotFoundException("Invalid token type for refresh");
        }
        String jti = claims.getId();
        String username = claims.getSubject();

        RefreshToken stored = refreshTokenRepository.findByToken(jti)
                .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token not found"));

        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new TokenRefreshException(refreshToken, "Refresh token expired");
        }

        if (!stored.getUser().getUsername().equals(username)) {
            throw new TokenRefreshException(refreshToken, "Subject mismatch");
        }


        return true;

    }

    public String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)  // match your production setting
                .path("/")
                .maxAge(0)     // expire immediately
                .sameSite("Strict")
                .build();

        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")  // must match original path
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", deleteAccessCookie.toString());
        response.addHeader("Set-Cookie", deleteRefreshCookie.toString());
    }





}
