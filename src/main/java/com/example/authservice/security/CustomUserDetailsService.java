package com.example.authservice.security;

import com.example.authservice.model.User;
import com.example.authservice.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Constructor injection
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Fetch user from DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Convert Set<Role> to String[] for Spring Security
        String[] roles = user.getRoles()
                .stream()
                .map(role -> role.name().replace("ROLE_", ""))
                // Role.USER → "USER"
                .toArray(String[]::new);

        // Build UserDetails object
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.name())).toList()
                )
                // Accepts multiple roles
                .build();
    }
}
