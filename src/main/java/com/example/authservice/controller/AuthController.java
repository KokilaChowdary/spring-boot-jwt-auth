package com.example.authservice.controller;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.SignupRequest;
import com.example.authservice.model.Role;
import com.example.authservice.model.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // 🔹 SIGNUP
    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "Error: Username already taken";
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.ROLE_USER));

        userRepository.save(user);

        return "User registered successfully";
    }

    // 🔹 LOGIN
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername()).get();

        System.out.println("RAW: " + request.getPassword());
        System.out.println("ENCODED: " + user.getPassword());
        System.out.println("MATCHES: " +
                passwordEncoder.matches(request.getPassword(), user.getPassword())
        );

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        return jwtUtil.generateToken(request.getUsername());
    }

}
