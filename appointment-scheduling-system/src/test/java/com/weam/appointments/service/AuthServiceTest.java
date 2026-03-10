package com.weam.appointments.service;

import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setup() {
        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new SchemaInitializer().init();
        authService = new AuthService(new JdbcUserRepository());
    }

    @Test
    void validLoginShouldReturnTrue() {
        assertTrue(authService.login("admin", "admin123"));
    }

    @Test
    void invalidPasswordShouldReturnFalse() {
        assertFalse(authService.login("admin", "wrong"));
    }

    @Test
    void unknownUserShouldReturnFalse() {
        assertFalse(authService.login("unknown", "123"));
    }

    @Test
    void registerNewUserShouldReturnTrue() {
    	String u = "rana_" + System.nanoTime();
    	boolean created = authService.register(u, "123", "STUDENT");
    	assertTrue(created);
    	assertTrue(authService.login(u, "123"));
    }

    @Test
    void registerExistingUserShouldReturnFalse() {
        authService.register("duplicate", "123", "STUDENT");
        boolean secondAttempt = authService.register("duplicate", "123", "STUDENT");
        assertFalse(secondAttempt);
    }

    @Test
    void logoutShouldNotThrowException() {
        assertDoesNotThrow(() -> authService.logout("admin"));
    }
    @Test
    void loginWithNullUsernameShouldReturnFalse() {
        assertFalse(authService.login(null, "123"));
    }

    @Test
    void loginWithEmptyPasswordShouldReturnFalse() {
        assertFalse(authService.login("admin", ""));
    }
    @Test
    void addUserDuplicateShouldReturnFalse() {
        String u = "cov_test_" + System.nanoTime();
        assertTrue(authService.register(u, "123", "STUDENT"));
        assertFalse(authService.register(u, "456", "STUDENT"));
    }
    @Test
    void schemaInitShouldNotThrow() {
        SchemaInitializer schema = new SchemaInitializer();
        assertDoesNotThrow(schema::init);
    }
    @Test
    void addUserShouldReturnTrueWhenInserted() {
        JdbcUserRepository repo = new JdbcUserRepository();
        String u = "cov90_insert_" + System.nanoTime();
        boolean inserted = repo.addUser(u, "123", "STUDENT");
        assertTrue(inserted);
    }

}