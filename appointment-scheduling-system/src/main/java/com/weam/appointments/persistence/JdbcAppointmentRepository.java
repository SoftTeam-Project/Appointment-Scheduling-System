package com.weam.appointments.persistence;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAppointmentRepository implements AppointmentRepository {

    private static final String TABLE_APPOINTMENTS = "appointments";

    private static final String COL_ID = "id";
    private static final String COL_SLOT_ID = "slot_id";
    private static final String COL_USERNAME = "username";
    private static final String COL_APPOINTMENT_DATE = "appointment_date";
    private static final String COL_APPOINTMENT_TIME = "appointment_time";
    private static final String COL_DURATION_MINUTES = "duration_minutes";
    private static final String COL_PARTICIPANTS = "participants";
    private static final String COL_STATUS = "status";
    private static final String COL_TYPE = "type";

    private static final String STATUS_CONFIRMED = "Confirmed";

    private static final String SELECT_COLUMNS =
            COL_ID + ", " +
            COL_SLOT_ID + ", " +
            COL_USERNAME + ", " +
            COL_APPOINTMENT_DATE + ", " +
            COL_APPOINTMENT_TIME + ", " +
            COL_DURATION_MINUTES + ", " +
            COL_PARTICIPANTS + ", " +
            COL_STATUS + ", " +
            COL_TYPE;

    @Override
    public List<Appointment> findAllFutureAppointments() {
        String sql = "SELECT " + SELECT_COLUMNS +
                " FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COL_STATUS + " = ?" +
                " AND (" + COL_APPOINTMENT_DATE + " || ' ' || " + COL_APPOINTMENT_TIME + ") >= datetime('now')" +
                " ORDER BY " + COL_APPOINTMENT_DATE + ", " + COL_APPOINTMENT_TIME;

        List<Appointment> list = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, STATUS_CONFIRMED);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query failed", e);
        }

        return list;
    }

    @Override
    public boolean save(Appointment appointment) {
        String sql = "INSERT INTO " + TABLE_APPOINTMENTS + "(" +
                COL_SLOT_ID + ", " +
                COL_USERNAME + ", " +
                COL_APPOINTMENT_DATE + ", " +
                COL_APPOINTMENT_TIME + ", " +
                COL_DURATION_MINUTES + ", " +
                COL_PARTICIPANTS + ", " +
                COL_STATUS + ", " +
                COL_TYPE +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<Appointment> findById(int id) {
        String sql = "SELECT " + SELECT_COLUMNS +
                " FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COL_ID + " = ?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query failed", e);
        }

        return Optional.empty();
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COL_ID + " = ?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Appointment> findUpcomingAppointments(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT " + SELECT_COLUMNS +
                " FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COL_STATUS + " = ?" +
                " AND (" + COL_APPOINTMENT_DATE + " || ' ' || " + COL_APPOINTMENT_TIME + ") BETWEEN ? AND ?" +
                " ORDER BY " + COL_APPOINTMENT_DATE + ", " + COL_APPOINTMENT_TIME;

        List<Appointment> result = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, STATUS_CONFIRMED);
            ps.setString(2, formatDateTime(from));
            ps.setString(3, formatDateTime(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch upcoming appointments", e);
        }

        return result;
    }

    @Override
    public List<Appointment> findFutureAppointmentsByUser(String username) {
        String sql = "SELECT " + SELECT_COLUMNS +
                " FROM " + TABLE_APPOINTMENTS +
                " WHERE " + COL_USERNAME + " = ?" +
                " AND " + COL_STATUS + " = ?" +
                " AND (" + COL_APPOINTMENT_DATE + " || ' ' || " + COL_APPOINTMENT_TIME + ") >= datetime('now')" +
                " ORDER BY " + COL_APPOINTMENT_DATE + ", " + COL_APPOINTMENT_TIME;

        List<Appointment> list = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, STATUS_CONFIRMED);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query failed", e);
        }

        return list;
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        String typeValue = rs.getString(COL_TYPE);

        AppointmentType appointmentType = typeValue == null
                ? AppointmentType.INDIVIDUAL
                : AppointmentType.valueOf(typeValue);

        return new Appointment(
                rs.getInt(COL_ID),
                rs.getInt(COL_SLOT_ID),
                rs.getString(COL_USERNAME),
                rs.getString(COL_APPOINTMENT_DATE),
                rs.getString(COL_APPOINTMENT_TIME),
                rs.getInt(COL_DURATION_MINUTES),
                rs.getInt(COL_PARTICIPANTS),
                rs.getString(COL_STATUS),
                appointmentType
        );
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.toString().replace('T', ' ');
    }
}