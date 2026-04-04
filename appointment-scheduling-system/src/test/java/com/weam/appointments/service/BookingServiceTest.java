package com.weam.appointments.service;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentType;
import com.weam.appointments.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private BookingService bookingService;
    private int futureSlotId;

    @BeforeEach
    void setup() {

        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new java.io.File("test-db.sqlite").delete();
        new SchemaInitializer().init();

        try (Connection con = Db.getConnection();
             Statement st = con.createStatement()) {

            st.execute("INSERT INTO appointment_slots(slot_date, slot_time, capacity, booked_count) " +
                    "VALUES (date('now', '+1 day'), '12:00', 5, 0)");

            try (ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    futureSlotId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Could not insert future slot");
        }

        bookingService = new BookingService(
                new JdbcAppointmentRepository(),
                new JdbcSlotRepository()
        );
    }

    @Test
    void validDurationShouldReturnTrue() {
        assertTrue(bookingService.isValidDuration(60));
    }

    @Test
    void invalidDurationShouldReturnFalse() {
        assertFalse(bookingService.isValidDuration(150));
    }

    @Test
    void validParticipantsShouldReturnTrue() {
        assertTrue(bookingService.isValidParticipants(3));
    }

    @Test
    void invalidParticipantsShouldReturnFalse() {
        assertFalse(bookingService.isValidParticipants(10));
    }

    @Test
    void availableSlotShouldReturnTrue() {
        assertTrue(bookingService.isSlotAvailable(futureSlotId));
    }

    @Test
    void unavailableSlotShouldReturnFalse() {
        assertFalse(bookingService.isSlotAvailable(999));
    }

    @Test
    void validBookingShouldReturnTrue() {
        boolean result = bookingService.bookAppointment(
                futureSlotId, "admin", 60, 2, AppointmentType.GROUP
        );
        assertTrue(result);
    }

    @Test
    void bookingWithInvalidDurationShouldReturnFalse() {
        boolean result = bookingService.bookAppointment(
                futureSlotId, "admin", 180, 2, AppointmentType.GROUP
        );
        assertFalse(result);
    }

    @Test
    void bookingWithInvalidParticipantsShouldReturnFalse() {
        boolean result = bookingService.bookAppointment(
                futureSlotId, "admin", 60, 10, AppointmentType.GROUP
        );
        assertFalse(result);
    }

    @Test
    void bookingWithInvalidSlotShouldReturnFalse() {
        boolean result = bookingService.bookAppointment(
                999, "admin", 60, 2, AppointmentType.GROUP
        );
        assertFalse(result);
    }

    @Test
    void individualBookingWithTwoParticipantsShouldFail() {
        boolean result = bookingService.bookAppointment(
                futureSlotId, "admin", 30, 2, AppointmentType.INDIVIDUAL
        );
        assertFalse(result);
    }

    @Test
    void groupBookingWithOneParticipantShouldFail() {
        boolean result = bookingService.bookAppointment(
                futureSlotId, "admin", 30, 1, AppointmentType.GROUP
        );
        assertFalse(result);
    }

    @Test
    void urgentBookingLongerThanThirtyMinutesShouldFail() {
        boolean result = bookingService.bookAppointment(
                futureSlotId, "admin", 60, 1, AppointmentType.URGENT
        );
        assertFalse(result);
    }

    @Test
    void urgentBookingWithinThirtyMinutesShouldSucceed() {
        boolean result = bookingService.bookAppointment(
                futureSlotId, "admin", 30, 1, AppointmentType.URGENT
        );
        assertTrue(result);
    }

    @Test
    void cancelAppointment_shouldSucceedForFutureOwned() {
        assertTrue(bookingService.bookAppointment(
                futureSlotId, "admin", 30, 1, AppointmentType.INDIVIDUAL
        ));

        List<Appointment> apps = bookingService.findFutureAppointmentsByUser("admin");
        assertFalse(apps.isEmpty());
        int appId = apps.get(0).getId();

        assertTrue(bookingService.cancelAppointment(appId, "admin"));
        assertTrue(bookingService.isSlotAvailable(futureSlotId));
    }

    @Test
    void cancelAppointment_shouldFailForPastAppointment() {
        String insertSql = "INSERT INTO appointments(slot_id, username, appointment_date, appointment_time, " +
                "duration_minutes, participants, status, type) VALUES (?, ?, '2000-01-01', '10:00', 30, 1, 'Confirmed', 'INDIVIDUAL')";
        int pastAppointmentId = -1;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, futureSlotId);
            ps.setString(2, "admin");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pastAppointmentId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            fail("Could not insert past appointment");
        }

        assertFalse(bookingService.cancelAppointment(pastAppointmentId, "admin"));
    }

    @Test
    void cancelAppointment_shouldFailIfNotOwner() {
        assertTrue(bookingService.bookAppointment(
                futureSlotId, "admin", 30, 1, AppointmentType.INDIVIDUAL
        ));

        List<Appointment> apps = bookingService.findFutureAppointmentsByUser("admin");
        int appId = apps.get(0).getId();

        assertFalse(bookingService.cancelAppointment(appId, "student"));
    }

    @Test
    void cancelAppointment_shouldFailIfAlreadyCancelledOrInvalidId() {
        assertFalse(bookingService.cancelAppointment(99999, "admin"));
    }

    @Test
    void adminCancelAppointment_shouldSucceedForAnyFuture() {
        assertTrue(bookingService.bookAppointment(
                futureSlotId, "student", 30, 1, AppointmentType.INDIVIDUAL
        ));

        List<Appointment> apps = bookingService.findAllFutureAppointments();
        assertFalse(apps.isEmpty());
        int appId = apps.get(0).getId();

        assertTrue(bookingService.adminCancelAppointment(appId));
        assertTrue(bookingService.isSlotAvailable(futureSlotId));
    }

    @Test
    void adminCancelAppointment_shouldFailForInvalidId() {
        assertFalse(bookingService.adminCancelAppointment(99999));
    }
}