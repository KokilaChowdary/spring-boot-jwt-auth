package com.example.authservice.service;

import com.example.authservice.model.RefreshToken;
import com.example.authservice.model.User;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // refresh token validity: 7 days
    private final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    // 🔹 Create refresh token
    public RefreshToken createRefreshToken(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // delete old refresh tokens (one active token per user)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(
                Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    // 🔹 Validate refresh token
    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }

        return token;
    }

    // 🔹 Find token
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    // 🔹 Logout
    public void deleteByUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.deleteByUser(user);
    }
}
