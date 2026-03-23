package com.weam.appointments.persistence;

import com.weam.appointments.domain.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcAppointmentRepository implements AppointmentRepository {

    @Override
    public boolean save(Appointment appointment) {
        String sql = """
                INSERT INTO appointments(slot_id, username,appointment_date, appointment_time, duration_minutes, participants, status)
                VALUES (?, ?, ?, ?, ? ,? ,?)
                """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, appointment.getSlotId());
            ps.setString(2, appointment.getUsername());
            ps.setString(3, appointment.getDate());
            ps.setString(4, appointment.getTime());
            ps.setInt(5, appointment.getDurationMinutes());
            ps.setInt(6, appointment.getParticipants());
            ps.setString(7, appointment.getStatus());

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
        	e.printStackTrace();
            return false;
        }
    }
    @Override
    public List<Appointment> findUpcomingAppointments(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT id, slot_id, username, appointment_date, appointment_time,
                   duration_minutes, participants, status
            FROM appointments
            WHERE status = 'Confirmed'
              AND (appointment_date || ' ' || appointment_time) BETWEEN ? AND ?
            ORDER BY appointment_date, appointment_time
            """;
        List<Appointment> result = new ArrayList<>();
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, from.toString().replace('T', ' '));
            ps.setString(2, to.toString().replace('T', ' '));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Appointment(
                        rs.getInt("id"),
                        rs.getInt("slot_id"),
                        rs.getString("username"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getInt("duration_minutes"),
                        rs.getInt("participants"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch upcoming appointments", e);
        }
        return result;
    }
    
}