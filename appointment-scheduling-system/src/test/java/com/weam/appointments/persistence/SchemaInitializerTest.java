package com.weam.appointments.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class SchemaInitializerTest {

    @BeforeEach
    void setup() {
        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new java.io.File("test-db.sqlite").delete();
    }

    @Test
    void initShouldCreateUsersTable() {
        assertDoesNotThrow(() -> new SchemaInitializer().init());

        assertDoesNotThrow(() -> {
            try (var con = Db.getConnection();
                 var st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table' AND name='users'"
                 )) {
                assertTrue(rs.next());
            }
        });
    }
}