package com.weam.appointments.service;

import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.persistence.UserRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setup() {
    	
        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new java.io.File("test-db.sqlite").delete();
        new SchemaInitializer().init();
        authService = new AuthService(new JdbcUserRepository());
    }

    @Test
    void validLoginShouldReturnUser() {
    	
        Optional<UserRecord> user = authService.login("admin", "admin123");
        assertTrue(user.isPresent());
        assertEquals("ADMIN", user.get().role());
    }

    @Test
    void invalidPasswordShouldReturnEmpty() {
    	
        assertFalse(authService.login("admin", "wrong").isPresent());
    }

    @Test
    void unknownUserShouldReturnEmpty() {
    	
        assertFalse(authService.login("unknown", "123").isPresent());
    }

    @Test
    void registerNewUserShouldReturnTrue() {
    	
        String u = "rana_" + System.nanoTime();
        boolean created = authService.register(u, "123", "STUDENT");
        assertTrue(created);
        Optional<UserRecord> loggedIn = authService.login(u, "123");
        assertTrue(loggedIn.isPresent());
        assertEquals("STUDENT", loggedIn.get().role());
    }

    @Test
    void registerExistingUserShouldReturnFalse() {
    	
        String u = "duplicate_" + System.nanoTime();
        assertTrue(authService.register(u, "123", "STUDENT"));
        boolean secondAttempt = authService.register(u, "123", "STUDENT");
        assertFalse(secondAttempt);
    }

    @Test
    void logoutShouldNotThrowException() {
    	
        assertDoesNotThrow(() -> authService.logout("admin"));
    }

    @Test
    void loginWithNullUsernameShouldReturnEmpty() {
    	
        assertFalse(authService.login(null, "123").isPresent());
    }

    @Test
    void loginWithEmptyPasswordShouldReturnEmpty() {
    	
        assertFalse(authService.login("admin", "").isPresent());
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