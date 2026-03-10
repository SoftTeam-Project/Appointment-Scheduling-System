package com.weam.appointments.persistence;

import com.weam.appointments.domain.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class JdbcAppointmentRepository implements AppointmentRepository {

    @Override
    public boolean save(Appointment appointment) {
        String sql = """
                INSERT INTO appointments(slot_id, username, duration_minutes, participants, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, appointment.getSlotId());
            ps.setString(2, appointment.getUsername());
            ps.setInt(3, appointment.getDurationMinutes());
            ps.setInt(4, appointment.getParticipants());
            ps.setString(5, appointment.getStatus());

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            return false;
        }
    }
}