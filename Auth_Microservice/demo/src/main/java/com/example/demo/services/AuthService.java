package com.example.demo.services;

import com.example.demo.entities.UserCredential;
import com.example.demo.repositories.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserCredentialRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserCredential register(UserCredential user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public String login(String username, String password) throws RuntimeException {
        UserCredential user = repository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtService.generateToken(user);
        }

        throw new RuntimeException("Invalid credentials");
    }

    public UserCredential updateUser(UUID id, UserCredential updated) {
        UserCredential user = repository.findById(id).orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        user.setUsername(updated.getUsername());
        user.setPassword(passwordEncoder.encode(updated.getPassword()));
        user.setRole(updated.getRole());

        return repository.save(user);
    }

    public UserCredential deleteUser(UUID id) {
        UserCredential user = repository.findById(id).orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        repository.deleteById(id);
        return user;
    }
}

