package com.example.file_transfert.controller;

import com.example.file_transfert.dto.user.UserCreate;
import com.example.file_transfert.dto.user.UserLogin;
import com.example.file_transfert.model.User;
import com.example.file_transfert.security.service.JwtService;
import com.example.file_transfert.service.interfaces.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private IUserService userService;
    @Autowired
    private JwtService jwtService;

    @Value("${MODE:dev}")
    private String mode;

    private final Pattern passwordRegex = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$");
    private final Pattern emailRegex = Pattern.compile("^(.+)@(\\S+)$");

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserLogin userLoginDto, HttpServletResponse response) {
        var user = userService.login(userLoginDto);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(mode.equals("production"))
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofDays(15))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok(Map.of("access_token", accessToken));
    }

    @PostMapping(value = "/refresh", produces = "application/json")
    public ResponseEntity<Map<String, String>> refresh(@CookieValue(value = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) return ResponseEntity.status(401).body(Map.of("error", "Missing refresh token"));

        String username = jwtService.extractUsernameFromRefresh(refreshToken);
        User user = userService.getByUsername(username);

        if (user == null || !jwtService.isValidRefreshToken(refreshToken, user) || jwtService.isRefreshTokenExpired(refreshToken)) {
            ResponseCookie clear = ResponseCookie.from("refresh_token")
                    .httpOnly(true)
                    .secure(mode.equals("production"))
                    .sameSite("Lax")
                    .path("/api/auth")
                    .maxAge(0)
                    .build();
            response.setHeader(HttpHeaders.SET_COOKIE, clear.toString());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        //Check if refresh expire in less than a day and refresh it
        if (jwtService.getRefreshTokenRemainingDays(refreshToken) <= 1) {
            String newRefreshToken = jwtService.generateRefreshToken(user);
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                    .httpOnly(true)
                    .secure(mode.equals("production"))
                    .sameSite("Lax")
                    .path("/api/auth")
                    .maxAge(Duration.ofDays(15))
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }
        return ResponseEntity.ok(Map.of("access_token", newAccessToken));
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<Map<String,String>> logout(HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        ResponseCookie clear = ResponseCookie.from("refresh_token")
                .httpOnly(true)
                .secure(mode.equals("production"))
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(0)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, clear.toString());
        return ResponseEntity.ok(Map.of("data", "Logout successful."));
    }

    @PostMapping(value = "/register", produces = "application/json")
    public ResponseEntity<Map<String, String>> register(@RequestBody UserCreate userCreateDto, HttpServletResponse response) {
        try {
            Matcher passMatcher = userCreateDto.getPassword() != null ? passwordRegex.matcher(userCreateDto.getPassword()) : null;
            if (passMatcher == null || !passMatcher.matches()) {
                return ResponseEntity.status(400).body(Map.of("error", "Password does not meet the required criteria"));
            }
            Matcher mailMatcher = userCreateDto.getEmail() != null ? emailRegex.matcher(userCreateDto.getEmail()) : null;
            if (mailMatcher == null || !mailMatcher.matches()) {
                return ResponseEntity.status(400).body(Map.of("error", "Email is invalid"));
            }
            userService.create(userCreateDto);
            UserLogin newUser = new UserLogin(userCreateDto.getUsername(), userCreateDto.getPassword());
            return login(newUser, response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
