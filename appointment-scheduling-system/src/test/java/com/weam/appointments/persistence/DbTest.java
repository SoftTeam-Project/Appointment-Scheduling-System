package com.weam.appointments.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class DbTest {

    @BeforeEach
    void setup() {
        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new java.io.File("test-db.sqlite").delete();
        new SchemaInitializer().init();
    }

    @Test
    void getConnectionShouldReturnConnection() {
        assertDoesNotThrow(() -> {
            try (Connection con = Db.getConnection()) {
                assertNotNull(con);
                assertFalse(con.isClosed());
            }
        });
    }
}