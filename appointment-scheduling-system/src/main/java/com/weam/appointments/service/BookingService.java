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
        if (type == AppointmentType.INDIVIDUAL && participants != 1) {
            return false;
        }

        if (type == AppointmentType.GROUP && participants < 2) {
            return false;
        }

        if (type == AppointmentType.URGENT && durationMinutes > 30) {
            return false;
        }

        return true;
    }

    public boolean bookAppointment(int slotId, String username, int durationMinutes, int participants, AppointmentType type) {
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

        AppointmentSlot slot = slotOpt.get();

        Appointment appointment = new Appointment(
            0,
            slotId,
            username,
            slot.date,
            slot.time,
            durationMinutes,
            participants,
            "Confirmed",
            type
        );

        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (!saveAppointmentWithConnection(appointment, con)) {
                    throw new SQLException("Save failed");
                }

                if (!incrementBookedCountWithConnection(slotId, con)) {
                    throw new SQLException("Increment failed");
                }

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

    public boolean cancelAppointment(int appointmentId, String username) {
        Optional<Appointment> appOpt = appointmentRepository.findById(appointmentId);
        if (appOpt.isEmpty()) {
            return false;
        }

        Appointment app = appOpt.get();

        if (!app.getUsername().equals(username)) {
            return false;
        }

        LocalDateTime appointmentDateTime = LocalDateTime.parse(app.getDate() + "T" + app.getTime());
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            return false;
        }

        int slotId = app.getSlotId();

        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);

            if (!deleteAppointmentWithConnection(appointmentId, con)) {
                throw new SQLException("Delete failed");
            }

            if (!decrementBookedCountWithConnection(slotId, con)) {
                throw new SQLException("Decrement failed");
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public List<Appointment> findFutureAppointmentsByUser(String username) {
        return appointmentRepository.findFutureAppointmentsByUser(username);
    }

    private boolean saveAppointmentWithConnection(Appointment app, Connection con) throws SQLException {
        String sql = "INSERT INTO appointments(slot_id, username, appointment_date, appointment_time, " +
                     "duration_minutes, participants, status, type) VALUES (?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
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
        String sql = "UPDATE appointment_slots SET booked_count = booked_count + 1 WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            return ps.executeUpdate() == 1;
        }
    }

    private boolean deleteAppointmentWithConnection(int id, Connection con) throws SQLException {
        String sql = "DELETE FROM appointments WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean adminCancelAppointment(int appointmentId) {
        Optional<Appointment> appOpt = appointmentRepository.findById(appointmentId);
        if (appOpt.isEmpty()) {
            return false;
        }

        Appointment app = appOpt.get();
        int slotId = app.getSlotId();

        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);

            if (!deleteAppointmentWithConnection(appointmentId, con)) {
                throw new SQLException("Delete failed");
            }

            if (!decrementBookedCountWithConnection(slotId, con)) {
                throw new SQLException("Decrement failed");
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Appointment> findAllFutureAppointments() {
        return appointmentRepository.findAllFutureAppointments();
    }

    private boolean decrementBookedCountWithConnection(int slotId, Connection con) throws SQLException {
        String sql = "UPDATE appointment_slots SET booked_count = booked_count - 1 WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            return ps.executeUpdate() == 1;
        }
    }
}