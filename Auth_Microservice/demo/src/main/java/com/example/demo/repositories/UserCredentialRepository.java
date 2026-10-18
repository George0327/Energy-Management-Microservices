package com.example.demo.repositories;

import com.example.demo.entities.UserCredential;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {
    UserCredential findByUsername(String username);
}
