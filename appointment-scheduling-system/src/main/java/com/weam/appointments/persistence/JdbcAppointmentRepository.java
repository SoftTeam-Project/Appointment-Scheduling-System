package com.weam.appointments.persistence;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private static final String SQL_SELECT = "SELECT ";
    private static final String SQL_FROM = " FROM ";
    private static final String SQL_WHERE = " WHERE ";
    private static final String SQL_AND = " AND ";
    private static final String SQL_ORDER_BY = " ORDER BY ";
    private static final String SQL_EQUALS_PARAM = " = ?";
    private static final String SQL_DATE_SEPARATOR = " || ' ' || ";
    private static final String SQL_COMMA_SPACE = ", ";

    private static final String DATE_TIME_EXPRESSION =
            "(" + COL_APPOINTMENT_DATE + SQL_DATE_SEPARATOR + COL_APPOINTMENT_TIME + ")";

    private static final String SELECT_COLUMNS =
            COL_ID + SQL_COMMA_SPACE +
            COL_SLOT_ID + SQL_COMMA_SPACE +
            COL_USERNAME + SQL_COMMA_SPACE +
            COL_APPOINTMENT_DATE + SQL_COMMA_SPACE +
            COL_APPOINTMENT_TIME + SQL_COMMA_SPACE +
            COL_DURATION_MINUTES + SQL_COMMA_SPACE +
            COL_PARTICIPANTS + SQL_COMMA_SPACE +
            COL_STATUS + SQL_COMMA_SPACE +
            COL_TYPE;

    @Override
    public List<Appointment> findAllFutureAppointments() {
        String sql = SQL_SELECT + SELECT_COLUMNS +
                SQL_FROM + TABLE_APPOINTMENTS +
                SQL_WHERE + COL_STATUS + SQL_EQUALS_PARAM +
                SQL_AND + DATE_TIME_EXPRESSION + " >= datetime('now')" +
                SQL_ORDER_BY + COL_APPOINTMENT_DATE + SQL_COMMA_SPACE + COL_APPOINTMENT_TIME;

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
                COL_SLOT_ID + SQL_COMMA_SPACE +
                COL_USERNAME + SQL_COMMA_SPACE +
                COL_APPOINTMENT_DATE + SQL_COMMA_SPACE +
                COL_APPOINTMENT_TIME + SQL_COMMA_SPACE +
                COL_DURATION_MINUTES + SQL_COMMA_SPACE +
                COL_PARTICIPANTS + SQL_COMMA_SPACE +
                COL_STATUS + SQL_COMMA_SPACE +
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
        String sql = SQL_SELECT + SELECT_COLUMNS +
                SQL_FROM + TABLE_APPOINTMENTS +
                SQL_WHERE + COL_ID + SQL_EQUALS_PARAM;

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
                SQL_WHERE + COL_ID + SQL_EQUALS_PARAM;

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
        String sql = SQL_SELECT + SELECT_COLUMNS +
                SQL_FROM + TABLE_APPOINTMENTS +
                SQL_WHERE + COL_STATUS + SQL_EQUALS_PARAM +
                SQL_AND + DATE_TIME_EXPRESSION + " BETWEEN ? AND ?" +
                SQL_ORDER_BY + COL_APPOINTMENT_DATE + SQL_COMMA_SPACE + COL_APPOINTMENT_TIME;

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
        String sql = SQL_SELECT + SELECT_COLUMNS +
                SQL_FROM + TABLE_APPOINTMENTS +
                SQL_WHERE + COL_USERNAME + SQL_EQUALS_PARAM +
                SQL_AND + COL_STATUS + SQL_EQUALS_PARAM +
                SQL_AND + DATE_TIME_EXPRESSION + " >= datetime('now')" +
                SQL_ORDER_BY + COL_APPOINTMENT_DATE + SQL_COMMA_SPACE + COL_APPOINTMENT_TIME;

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

        return Appointment.builder()
                .id(rs.getInt(COL_ID))
                .slotId(rs.getInt(COL_SLOT_ID))
                .username(rs.getString(COL_USERNAME))
                .date(rs.getString(COL_APPOINTMENT_DATE))
                .time(rs.getString(COL_APPOINTMENT_TIME))
                .durationMinutes(rs.getInt(COL_DURATION_MINUTES))
                .participants(rs.getInt(COL_PARTICIPANTS))
                .status(rs.getString(COL_STATUS))
                .type(appointmentType)
                .build();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.toString().replace('T', ' ');
    }
}