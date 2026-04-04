package com.weam.appointments.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {

	private void upgradeAppointmentsTable(Statement st) throws SQLException {
	    // تحقق مما إذا كان العمود appointment_date موجودًا
	    ResultSet rs = st.executeQuery("PRAGMA table_info(appointments)");
	    boolean hasDateColumn = false;
	    boolean hasTimeColumn = false;
	    while (rs.next()) {
	        String colName = rs.getString("name");
	        if ("appointment_date".equals(colName)) hasDateColumn = true;
	        if ("appointment_time".equals(colName)) hasTimeColumn = true;
	    }
	    rs.close();

	    if (!hasDateColumn) {
	        st.execute("ALTER TABLE appointments ADD COLUMN appointment_date TEXT NOT NULL DEFAULT ''");
	    }
	    if (!hasTimeColumn) {
	        st.execute("ALTER TABLE appointments ADD COLUMN appointment_time TEXT NOT NULL DEFAULT ''");
	    }
	}
	
	
    public void init() {

        try (Connection con = Db.getConnection();
             Statement st = con.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL ,
                    email TEXT
                );
            """);
            st.execute("""
            	    INSERT OR IGNORE INTO users(username, password, role, email)
            	    VALUES ('admin', 'admin123', 'ADMIN', 'hamodyalomari7@gmail.com'),
            	           ('student', 'stud123', 'STUDENT', 'alimashaqi2002@gmail.com');
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
            upgradeAppointmentsTable(st);
            st.execute("""
                    INSERT INTO appointments(slot_id, username, appointment_date, appointment_time,
                                             duration_minutes, participants, status)
                    VALUES (1, 'student', '2026-03-02', '10:00', 30, 2, 'Confirmed'),
                           (2, 'admin', '2026-03-02', '14:00', 60, 1, 'Confirmed');
                """);

        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }
}