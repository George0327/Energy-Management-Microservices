package com.example.demo.controllers;

import com.example.demo.entities.UserCredential;
import com.example.demo.services.AuthService;
import com.example.demo.services.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserCredential> register(@RequestBody UserCredential user) {
        UserCredential registeredUser = authService.register(user);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        String token = authService.login(request.get("username"), request.get("password"));
        return ResponseEntity.ok(token);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestHeader("Authorization") String token) {
        Claims claims = new JwtService().validateToken(token.replace("Bearer ", ""));
        return ResponseEntity.ok(Map.of("username", claims.getSubject(), "role", claims.get("role")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserCredential> updateUser(@PathVariable UUID id, @RequestBody UserCredential updated) {
        return ResponseEntity.ok(authService.updateUser(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserCredential> deleteUser(@PathVariable UUID id) {
        UserCredential deletedUser = authService.deleteUser(id);
        return ResponseEntity.ok(deletedUser);
    }
}

