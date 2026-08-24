package com.example.file_transfert.security.service;

import com.example.file_transfert.model.Role;
import com.example.file_transfert.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String jwtSecret;
    @Value("${JWT_REFRESH_SECRET}")
    private String jwtRefreshSecret;

    private SecretKey getSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        long expirationTime = 60 * 60 * 1000; // 1 hour in milliseconds
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", user.getRoles().stream()
                        .map(Role::getName).toList())
                .claim("authorities", authorities)
                .audience().add("access").and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(jwtSecret), Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshToken(User user) {
        long expirationTime = 15 * 24 * 60 * 60 * 1000; // 15 jours
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(user.getUsername())
                .audience().add("refresh").and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(jwtRefreshSecret), Jwts.SIG.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey(jwtSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractUsernameFromRefresh(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey(jwtRefreshSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey(jwtSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public List<String> extractRoles(String token) {
        return extractClaims(token).get("roles", List.class);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public boolean isValidAccessToken(String token, UserDetails user) {
        try {
            String audience = Jwts.parser()
                    .verifyWith(getSigningKey(jwtSecret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getAudience().stream().findFirst().orElse(null);

            String username = extractUsername(token);
            return Objects.equals(audience, "access") && Objects.equals(username, user.getUsername());
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isValidRefreshToken(String token, UserDetails user) {
        try {
            String audience = Jwts.parser()
                    .verifyWith(getSigningKey(jwtRefreshSecret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getAudience().stream().findFirst().orElse(null);

            return Objects.equals(audience, "refresh");
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(getSigningKey(jwtSecret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true; // If the token is invalid, consider it expired
        }
    }

    public boolean isRefreshTokenExpired(String refreshToken) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(getSigningKey(jwtRefreshSecret))
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true; // If the refreshToken is invalid, consider it expired
        }
    }

    public Date extractRefreshExpiration(String refreshToken) {
        return Jwts.parser()
                .verifyWith(getSigningKey(jwtRefreshSecret))
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getExpiration();
    }

    public long getRefreshTokenRemainingMs(String refreshToken) {
        Date expiration = extractRefreshExpiration(refreshToken);
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public long getRefreshTokenRemainingDays(String refreshToken) {
        long remainingMs = getRefreshTokenRemainingMs(refreshToken);
        return TimeUnit.MILLISECONDS.toDays(remainingMs); // jours entiers restants
    }

    public List<String> extractAuthorities(String token) {
        Claims claims = extractClaims(token);
        List<String> authorities = claims.get("authorities", List.class);
        return authorities != null ? authorities : new ArrayList<>();
    }
}
