package com.weam.appointments.service;

import com.weam.appointments.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private BookingService bookingService;

    @BeforeEach
    void setup() {
        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new java.io.File("test-db.sqlite").delete();
        new SchemaInitializer().init();

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
        assertTrue(bookingService.isSlotAvailable(1));
    }

    @Test
    void unavailableSlotShouldReturnFalse() {
        assertFalse(bookingService.isSlotAvailable(999));
    }

    @Test
    void validBookingShouldReturnTrue() {
        boolean result = bookingService.bookAppointment(1, "admin", 60, 2);
        assertTrue(result);
    }

    @Test
    void bookingWithInvalidDurationShouldReturnFalse() {
        boolean result = bookingService.bookAppointment(1, "admin", 180, 2);
        assertFalse(result);
    }

    @Test
    void bookingWithInvalidParticipantsShouldReturnFalse() {
        boolean result = bookingService.bookAppointment(1, "admin", 60, 10);
        assertFalse(result);
    }

    @Test
    void bookingWithInvalidSlotShouldReturnFalse() {
        boolean result = bookingService.bookAppointment(999, "admin", 60, 2);
        assertFalse(result);
    }
}