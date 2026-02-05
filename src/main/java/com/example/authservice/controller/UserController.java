package com.example.authservice.controller;

import com.example.authservice.dto.UserResponse;
import com.example.authservice.model.User;
import com.example.authservice.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ GET ALL USERS (SAFE RESPONSE)
    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> {
                    UserResponse res = new UserResponse();
                    res.setId(user.getId());
                    res.setUsername(user.getUsername());
                    res.setEmail(user.getEmail());
                    res.setRoles(
                            user.getRoles()
                                    .stream()
                                    .map(Enum::name)
                                    .collect(Collectors.toSet())
                    );
                    return res;
                })
                .collect(Collectors.toList());
    }

    // ✅ GET USER COUNT
    @GetMapping("/count")
    public long getUserCount() {
        return userRepository.count();
    }
}
