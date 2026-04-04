package com.weam.appointments.service;

import com.weam.appointments.persistence.UserRecord;
import com.weam.appointments.persistence.UserRepository;
import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserRecord> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.password().equals(password));
    }

    public void logout(String username) {
        System.out.println(username + " logged out");
    }

    public boolean register(String username, String password, String role) {
        return userRepository.addUser(username, password, role);
    }
}