package com.weam.appointments.service;

import com.weam.appointments.persistence.UserRecord;
import com.weam.appointments.persistence.UserRepository;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================
    // LOGIN
    // =========================
    public Optional<UserRecord> login(String username, String password) {

        return userRepository.findByUsername(username)
                .filter(user -> user.password().equals(password));
    }

    // =========================
    // REGISTER
    // =========================
    public boolean register(String username,
                            String password,
                            String role,
                            String email) {

        return userRepository.addUser(
                username,
                password,
                role,
                email
        );
    }

    // =========================
    // LOGOUT
    // =========================
    public void logout(String username) {
        System.out.println(username + " logged out");
    }
}