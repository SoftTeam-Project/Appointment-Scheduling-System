package com.weam.appointments.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentSlotTest {

    @Test
    void isAvailableShouldReturnTrueWhenBookedLessThanCapacity() {
        AppointmentSlot s = new AppointmentSlot(1, "2026-03-01", "10:00", 2, 1);
        assertTrue(s.isAvailable());
    }

    @Test
    void isAvailableShouldReturnFalseWhenFullyBooked() {
        AppointmentSlot s = new AppointmentSlot(1, "2026-03-01", "10:00", 1, 1);
        assertFalse(s.isAvailable());
    }
}