package com.example.authservice.service;

import com.example.authservice.dto.*;
import com.example.authservice.model.*;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;
import com.example.authservice.exception.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;

@Service
public class AuthService {


    private static final Logger logger =
            LoggerFactory.getLogger(AuthService.class);
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
        logger.info("User {} registered successfully", request.getUsername());
        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));


        if (user.isAccountLocked()) {

            if (user.getLockTime().plusMinutes(15)
                    .isBefore(java.time.LocalDateTime.now())) {

                user.setAccountLocked(false);
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);

            } else {
                throw new AccountLockedException(
                        "Account is locked. Try again after 15 minutes."
                );
            }
        }

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

        } catch (Exception ex) {

            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);

            if (attempts >= 5) {
                user.setAccountLocked(true);
                user.setLockTime(java.time.LocalDateTime.now());
            }

            userRepository.save(user);

            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }

        // ✅ Successful login → reset attempts
        user.setFailedAttempts(0);
        userRepository.save(user);

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

        logger.info("Access token refreshed for {}", user.getUsername());

        return new AuthResponse(newAccessToken, requestToken);
    }

    public void logout(String username) {

        refreshTokenService.deleteByUser(username);
        logger.info("User {} logged out", username);
    }
}