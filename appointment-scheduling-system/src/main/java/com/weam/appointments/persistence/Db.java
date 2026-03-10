package com.weam.appointments.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    private static final String DEFAULT_URL = "jdbc:sqlite:db.sqlite";

    public static Connection getConnection() throws SQLException {
        String url = System.getProperty("db.url", DEFAULT_URL);
        return DriverManager.getConnection(url);
    }
}