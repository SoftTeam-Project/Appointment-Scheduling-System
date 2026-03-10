package com.weam.appointments.service;

import com.weam.appointments.domain.AppointmentSlot;
import com.weam.appointments.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlotServiceTest {

    @BeforeEach
    void setup() {
        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new java.io.File("test-db.sqlite").delete();
        new SchemaInitializer().init();
    }

    @Test
    void viewAvailableSlotsShouldReturnList() {
        SlotService service = new SlotService(new JdbcSlotRepository());
        List<AppointmentSlot> slots = service.viewAvailableSlots();

        assertNotNull(slots);
        assertTrue(slots.stream().allMatch(AppointmentSlot::isAvailable));
    }
}