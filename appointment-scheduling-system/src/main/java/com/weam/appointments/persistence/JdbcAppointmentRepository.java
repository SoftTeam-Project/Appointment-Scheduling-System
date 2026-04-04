package com.weam.appointments.persistence;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAppointmentRepository implements AppointmentRepository {

    @Override
    public List<Appointment> findAllFutureAppointments() {
        String sql = "SELECT id, slot_id, username, appointment_date, appointment_time, duration_minutes, participants, status, type " +
                     "FROM appointments WHERE status = 'Confirmed' AND (appointment_date || ' ' || appointment_time) >= datetime('now') " +
                     "ORDER BY appointment_date, appointment_time";

        List<Appointment> list = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Appointment(
                    rs.getInt("id"),
                    rs.getInt("slot_id"),
                    rs.getString("username"),
                    rs.getString("appointment_date"),
                    rs.getString("appointment_time"),
                    rs.getInt("duration_minutes"),
                    rs.getInt("participants"),
                    rs.getString("status"),
                    rs.getString("type") == null
                        ? AppointmentType.INDIVIDUAL
                        : AppointmentType.valueOf(rs.getString("type"))
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("DB query failed", e);
        }

        return list;
    }

    @Override
    public boolean save(Appointment appointment) {
        String sql = """
            INSERT INTO appointments(slot_id, username, appointment_date, appointment_time,
                                     duration_minutes, participants, status, type)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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
            ps.setString(8, appointment.getType().name());

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<Appointment> findById(int id) {
        String sql = "SELECT id, slot_id, username, appointment_date, appointment_time, " +
                     "duration_minutes, participants, status, type FROM appointments WHERE id = ?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Appointment(
                        rs.getInt("id"),
                        rs.getInt("slot_id"),
                        rs.getString("username"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getInt("duration_minutes"),
                        rs.getInt("participants"),
                        rs.getString("status"),
                        rs.getString("type") == null
                            ? AppointmentType.INDIVIDUAL
                            : AppointmentType.valueOf(rs.getString("type"))
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("DB query failed", e);
        }

        return Optional.empty();
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM appointments WHERE id = ?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
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
                   duration_minutes, participants, status, type
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
                        rs.getString("status"),
                        rs.getString("type") == null
                            ? AppointmentType.INDIVIDUAL
                            : AppointmentType.valueOf(rs.getString("type"))
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch upcoming appointments", e);
        }

        return result;
    }

    @Override
    public List<Appointment> findFutureAppointmentsByUser(String username) {
        String sql = """
            SELECT id, slot_id, username, appointment_date, appointment_time,
                   duration_minutes, participants, status, type
            FROM appointments
            WHERE username = ?
              AND status = 'Confirmed'
              AND (appointment_date || ' ' || appointment_time) >= datetime('now')
            ORDER BY appointment_date, appointment_time
            """;

        List<Appointment> list = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Appointment(
                        rs.getInt("id"),
                        rs.getInt("slot_id"),
                        rs.getString("username"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getInt("duration_minutes"),
                        rs.getInt("participants"),
                        rs.getString("status"),
                        rs.getString("type") == null
                            ? AppointmentType.INDIVIDUAL
                            : AppointmentType.valueOf(rs.getString("type"))
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("DB query failed", e);
        }

        return list;
    }
}