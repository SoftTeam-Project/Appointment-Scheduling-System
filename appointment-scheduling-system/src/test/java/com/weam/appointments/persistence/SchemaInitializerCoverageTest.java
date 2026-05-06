package com.weam.appointments.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaInitializerCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void init_shouldCreateMainTablesAndSeedData() throws Exception {
        Path dbPath = tempDir.resolve("schema-init-test.sqlite");
        System.setProperty("db.url", "jdbc:sqlite:" + dbPath);

        new SchemaInitializer().init();

        try (Connection con = Db.getConnection();
             Statement st = con.createStatement()) {

            assertTrue(tableExists(st, "users"));
            assertTrue(tableExists(st, "appointment_slots"));
            assertTrue(tableExists(st, "appointments"));

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 2);
            }

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM appointment_slots")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 3);
            }
        }
    }

    @Test
    void init_shouldUpgradeOldAppointmentsTableByAddingMissingColumns() throws Exception {
        Path dbPath = tempDir.resolve("schema-upgrade-test.sqlite");
        System.setProperty("db.url", "jdbc:sqlite:" + dbPath);

        createOldAppointmentsTableWithoutNewColumns();

        new SchemaInitializer().init();

        try (Connection con = Db.getConnection();
             Statement st = con.createStatement()) {

            assertTrue(columnExists(st, "appointments", "appointment_date"));
            assertTrue(columnExists(st, "appointments", "appointment_time"));
            assertTrue(columnExists(st, "appointments", "type"));
        }
    }

    private void createOldAppointmentsTableWithoutNewColumns() throws Exception {
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement()) {

            st.execute("""
                    CREATE TABLE appointments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        slot_id INTEGER NOT NULL,
                        username TEXT NOT NULL,
                        duration_minutes INTEGER NOT NULL,
                        participants INTEGER NOT NULL,
                        status TEXT NOT NULL
                    );
                    """);

            assertFalse(columnExists(st, "appointments", "appointment_date"));
            assertFalse(columnExists(st, "appointments", "appointment_time"));
            assertFalse(columnExists(st, "appointments", "type"));
        }
    }

    private boolean tableExists(Statement st, String tableName) throws Exception {
        try (ResultSet rs = st.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'"
        )) {
            return rs.next();
        }
    }

    private boolean columnExists(Statement st, String tableName, String columnName) throws Exception {
        try (ResultSet rs = st.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (rs.next()) {
                if (columnName.equals(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }
}