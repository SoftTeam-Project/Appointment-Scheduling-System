package com.weam.appointments.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    void validLoginShouldReturnTrue() {
        AuthService authService = new AuthService();
        assertTrue(authService.login("admin", "admin123"));
    }

    @Test
    void invalidPasswordShouldReturnFalse() {
        AuthService authService = new AuthService();
        assertFalse(authService.login("admin", "wrong"));
    }

    @Test
    void unknownUserShouldReturnFalse() {
        AuthService authService = new AuthService();
        assertFalse(authService.login("unknown", "123"));
    }
}