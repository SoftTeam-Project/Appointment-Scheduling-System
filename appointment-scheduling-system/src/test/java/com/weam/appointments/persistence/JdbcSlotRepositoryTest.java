package com.weam.appointments.persistence;

import com.weam.appointments.domain.AppointmentSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcSlotRepositoryTest {

    @BeforeEach
    void setup() {
        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new java.io.File("test-db.sqlite").delete();
        new SchemaInitializer().init();
    }

    @Test
    void findAvailableSlotsShouldReturnOnlyAvailable() {
        SlotRepository repo = new JdbcSlotRepository();

        List<AppointmentSlot> slots = repo.findAvailableSlots();
        assertNotNull(slots);

        assertTrue(slots.stream().allMatch(AppointmentSlot::isAvailable));
    }
}