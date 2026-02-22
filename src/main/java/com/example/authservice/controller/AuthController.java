package com.example.authservice.controller;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.SignupRequest;
import com.example.authservice.dto.AuthResponse;
import com.example.authservice.service.AuthService;
import com.example.authservice.dto.*;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 🔹 SIGNUP
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        String response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 🔹 LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // 🔹 REFRESH TOKEN
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshRequest request) {

        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);

    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestParam String username) {

        authService.logout(username);
        return ResponseEntity.ok("Logged out successfully");
    }
}