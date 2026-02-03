package com.example.authservice.controller;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.SignupRequest;
import com.example.authservice.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
