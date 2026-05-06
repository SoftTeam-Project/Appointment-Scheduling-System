package com.weam.appointments.service;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentSlot;
import com.weam.appointments.domain.AppointmentType;
import com.weam.appointments.persistence.AppointmentRepository;
import com.weam.appointments.persistence.Db;
import com.weam.appointments.persistence.SlotRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BookingService {

    private static final String STATUS_CONFIRMED = "Confirmed";

    private static final String INSERT_APPOINTMENT_SQL =
            "INSERT INTO appointments(slot_id, username, appointment_date, appointment_time, " +
            "duration_minutes, participants, status, type) VALUES (?,?,?,?,?,?,?,?)";

    private static final String INCREMENT_BOOKED_COUNT_SQL =
            "UPDATE appointment_slots SET booked_count = booked_count + 1 WHERE id = ?";

    private static final String DECREMENT_BOOKED_COUNT_SQL =
            "UPDATE appointment_slots SET booked_count = booked_count - 1 WHERE id = ?";

    private static final String DELETE_APPOINTMENT_SQL =
            "DELETE FROM appointments WHERE id = ?";

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;

    public BookingService(AppointmentRepository appointmentRepository, SlotRepository slotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
    }

    public boolean isValidDuration(int durationMinutes) {
        return durationMinutes > 0 && durationMinutes <= 120;
    }

    public boolean isValidParticipants(int participants) {
        return participants > 0 && participants <= 5;
    }

    public boolean isSlotAvailable(int slotId) {
        List<AppointmentSlot> slots = slotRepository.findAvailableSlots();
        return slots.stream().anyMatch(slot -> slot.id == slotId);
    }

    public boolean isValidForType(AppointmentType type, int durationMinutes, int participants) {
        return (type != AppointmentType.INDIVIDUAL || participants == 1)
                && (type != AppointmentType.GROUP || participants >= 2)
                && (type != AppointmentType.URGENT || durationMinutes <= 30);
    }

    public boolean bookAppointment(int slotId,
                                   String username,
                                   int durationMinutes,
                                   int participants,
                                   AppointmentType type) {
        if (!isValidDuration(durationMinutes)) {
            return false;
        }

        if (!isValidParticipants(participants)) {
            return false;
        }

        if (!isValidForType(type, durationMinutes, participants)) {
            return false;
        }

        Optional<AppointmentSlot> slotOpt = slotRepository.findById(slotId);
        if (slotOpt.isEmpty() || !slotOpt.get().isAvailable()) {
            return false;
        }

        Appointment appointment = createAppointment(
                slotId,
                username,
                durationMinutes,
                participants,
                type,
                slotOpt.get()
        );

        return executeInTransaction(con -> {
            if (!saveAppointmentWithConnection(appointment, con)) {
                throw new SQLException("Save failed");
            }

            if (!incrementBookedCountWithConnection(slotId, con)) {
                throw new SQLException("Increment failed");
            }
        });
    }

    private Appointment createAppointment(int slotId,
                                          String username,
                                          int durationMinutes,
                                          int participants,
                                          AppointmentType type,
                                          AppointmentSlot slot) {
        return Appointment.builder()
                .id(0)
                .slotId(slotId)
                .username(username)
                .date(slot.date)
                .time(slot.time)
                .durationMinutes(durationMinutes)
                .participants(participants)
                .status(STATUS_CONFIRMED)
                .type(type)
                .build();
    }

    public boolean cancelAppointment(int appointmentId, String username) {
        Optional<Appointment> appOpt = appointmentRepository.findById(appointmentId);
        if (appOpt.isEmpty()) {
            return false;
        }

        Appointment appointment = appOpt.get();

        if (!appointment.getUsername().equals(username)) {
            return false;
        }

        if (isPastAppointment(appointment)) {
            return false;
        }

        return cancelAppointmentByIdAndSlot(appointmentId, appointment.getSlotId());
    }

    public boolean adminCancelAppointment(int appointmentId) {
        Optional<Appointment> appOpt = appointmentRepository.findById(appointmentId);
        if (appOpt.isEmpty()) {
            return false;
        }

        Appointment appointment = appOpt.get();
        return cancelAppointmentByIdAndSlot(appointmentId, appointment.getSlotId());
    }

    private boolean cancelAppointmentByIdAndSlot(int appointmentId, int slotId) {
        return executeInTransaction(con -> {
            if (!deleteAppointmentWithConnection(appointmentId, con)) {
                throw new SQLException("Delete failed");
            }

            if (!decrementBookedCountWithConnection(slotId, con)) {
                throw new SQLException("Decrement failed");
            }
        });
    }

    private boolean isPastAppointment(Appointment appointment) {
        LocalDateTime appointmentDateTime =
                LocalDateTime.parse(appointment.getDate() + "T" + appointment.getTime());

        return appointmentDateTime.isBefore(LocalDateTime.now());
    }

    public List<Appointment> findFutureAppointmentsByUser(String username) {
        return appointmentRepository.findFutureAppointmentsByUser(username);
    }

    public List<Appointment> findAllFutureAppointments() {
        return appointmentRepository.findAllFutureAppointments();
    }

    private boolean executeInTransaction(TransactionAction action) {
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);

            try {
                action.execute(con);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                return false;
            }

        } catch (SQLException e) {
            return false;
        }
    }

    private boolean saveAppointmentWithConnection(Appointment app, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERT_APPOINTMENT_SQL)) {
            ps.setInt(1, app.getSlotId());
            ps.setString(2, app.getUsername());
            ps.setString(3, app.getDate());
            ps.setString(4, app.getTime());
            ps.setInt(5, app.getDurationMinutes());
            ps.setInt(6, app.getParticipants());
            ps.setString(7, app.getStatus());
            ps.setString(8, app.getType().name());

            return ps.executeUpdate() == 1;
        }
    }

    private boolean incrementBookedCountWithConnection(int slotId, Connection con) throws SQLException {
        return updateBookedCount(slotId, con, INCREMENT_BOOKED_COUNT_SQL);
    }

    private boolean decrementBookedCountWithConnection(int slotId, Connection con) throws SQLException {
        return updateBookedCount(slotId, con, DECREMENT_BOOKED_COUNT_SQL);
    }

    private boolean updateBookedCount(int slotId, Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            return ps.executeUpdate() == 1;
        }
    }

    private boolean deleteAppointmentWithConnection(int id, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(DELETE_APPOINTMENT_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    @FunctionalInterface
    private interface TransactionAction {
        void execute(Connection con) throws SQLException;
    }
}