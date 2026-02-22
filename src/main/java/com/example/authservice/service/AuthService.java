package com.example.authservice.service;

import com.example.authservice.dto.*;
import com.example.authservice.model.*;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public String signup(SignupRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.ROLE_USER));

        userRepository.save(user);

        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateToken(user.getUsername());

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user.getUsername());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }

    public AuthResponse refresh(String requestToken) {

        RefreshToken refreshToken =
                refreshTokenService.findByToken(requestToken);

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        String newAccessToken =
                jwtUtil.generateToken(user.getUsername());

        return new AuthResponse(newAccessToken, requestToken);
    }

    public void logout(String username) {
        refreshTokenService.deleteByUser(username);
    }
}