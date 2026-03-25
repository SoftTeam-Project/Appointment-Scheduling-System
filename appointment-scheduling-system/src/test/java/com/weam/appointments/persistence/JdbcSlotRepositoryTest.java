package com.weam.appointments.persistence;

import com.weam.appointments.domain.AppointmentSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

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
    @Test
    void incrementBookedCount_shouldIncrease() {
    	
        SlotRepository repo = new JdbcSlotRepository();
        Optional<AppointmentSlot> slotOpt = repo.findById(1);
        assertTrue(slotOpt.isPresent());
        int before = slotOpt.get().bookedCount;
        assertTrue(repo.incrementBookedCount(1));
        slotOpt = repo.findById(1);
        assertEquals(before + 1, slotOpt.get().bookedCount);
        
        assertTrue(repo.decrementBookedCount(1));
    }

    @Test
    void decrementBookedCount_shouldDecrease() {
        // مشابه
    }
}