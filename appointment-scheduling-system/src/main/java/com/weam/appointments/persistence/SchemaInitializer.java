package com.weam.appointments.persistence;

import java.sql.Connection;
import java.sql.Statement;

public class SchemaInitializer {

    public void init() {

        try (Connection con = Db.getConnection();
             Statement st = con.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS appointment_slots (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    slot_date TEXT NOT NULL,
                    slot_time TEXT NOT NULL,
                    capacity INTEGER NOT NULL,
                    booked_count INTEGER NOT NULL DEFAULT 0
                );
            """);

            st.execute("""
                INSERT OR IGNORE INTO users(username, password, role)
                VALUES ('admin', 'admin123', 'ADMIN');
            """);

            st.execute("""
                INSERT OR IGNORE INTO users(username, password, role)
                VALUES ('student', 'stud123', 'STUDENT');
            """);

            st.execute("""
                INSERT INTO appointment_slots(slot_date, slot_time, capacity, booked_count)
                VALUES ('2026-03-01','10:00',1,0);
            """);
            st.execute("""
                INSERT INTO appointment_slots(slot_date, slot_time, capacity, booked_count)
                VALUES ('2026-03-01','10:30',1,0);
            """);
            st.execute("""
                INSERT INTO appointment_slots(slot_date, slot_time, capacity, booked_count)
                VALUES ('2026-03-01','11:00',1,1);
            """);
            st.execute("""
            	    CREATE TABLE IF NOT EXISTS appointments (
            	        id INTEGER PRIMARY KEY AUTOINCREMENT,
            	        slot_id INTEGER NOT NULL,
            	        username TEXT NOT NULL,
            	        duration_minutes INTEGER NOT NULL,
            	        participants INTEGER NOT NULL,
            	        status TEXT NOT NULL
            	    );
            	""");

        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }
}