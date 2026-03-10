package com.weam.appointments.service;

import com.weam.appointments.persistence.UserRepository;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String username, String password) {

        return userRepository.findByUsername(username)
                .map(user -> user.password().equals(password))
                .orElse(false);
    }

    public void logout(String username) {
        System.out.println(username + " logged out");
    }
    public boolean register(String username, String password, String role) {
        return userRepository.addUser(username, password, role);
    }
}