package com.weam.appointments.persistence;

import java.util.Optional;

public interface UserRepository {

    Optional<UserRecord> findByUsername(String username);

    boolean addUser(String username, String password, String role);

    boolean addUser(String username, String password, String role, String email);
}