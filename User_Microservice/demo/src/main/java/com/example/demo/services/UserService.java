package com.example.demo.services;

import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateUser(UUID id, User newUser){
        User existingUser = userRepository.findById(id).orElse(null);
        if(existingUser != null){
            existingUser.setUsername(newUser.getUsername());
            existingUser.setEmail(newUser.getEmail());
            existingUser.setRole(newUser.getRole());
            return userRepository.save(existingUser);
        }
        return null;
    }

    public User deleteUser(UUID id){
        User existingUser = userRepository.findById(id).orElse(null);
        if(existingUser != null){
            userRepository.deleteById(id);
            return existingUser;
        }
        return null;
    }
}
