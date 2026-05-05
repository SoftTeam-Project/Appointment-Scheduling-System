package com.weam.appointments.persistence;

import com.weam.appointments.domain.AppointmentSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcSlotRepositoryCoverageTest {

    @TempDir
    Path tempDir;

    private JdbcSlotRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        Path dbPath = tempDir.resolve("slot-repository-test.sqlite");
        System.setProperty("db.url", "jdbc:sqlite:" + dbPath);

        new SchemaInitializer().init();
        repository = new JdbcSlotRepository();
    }

    @Test
    void findAvailableSlots_shouldReturnOnlySlotsWithRemainingCapacity() {
        List<AppointmentSlot> slots = repository.findAvailableSlots();

        assertFalse(slots.isEmpty());
        assertTrue(slots.stream().allMatch(slot -> slot.bookedCount < slot.capacity));
    }

    @Test
    void findById_shouldReturnSlotWhenItExists() {
        Optional<AppointmentSlot> result = repository.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().id);
        assertEquals("2026-03-01", result.get().date);
        assertEquals("10:00", result.get().time);
    }

    @Test
    void findById_shouldReturnEmptyWhenSlotDoesNotExist() {
        Optional<AppointmentSlot> result = repository.findById(99999);

        assertTrue(result.isEmpty());
    }

    @Test
    void incrementBookedCount_shouldReturnTrueAndIncreaseBookedCount() {
        Optional<AppointmentSlot> before = repository.findById(1);
        assertTrue(before.isPresent());

        boolean updated = repository.incrementBookedCount(1);

        Optional<AppointmentSlot> after = repository.findById(1);
        assertTrue(after.isPresent());

        assertTrue(updated);
        assertEquals(before.get().bookedCount + 1, after.get().bookedCount);
    }

    @Test
    void incrementBookedCount_shouldReturnFalseWhenSlotDoesNotExist() {
        boolean updated = repository.incrementBookedCount(99999);

        assertFalse(updated);
    }

    @Test
    void decrementBookedCount_shouldReturnTrueAndDecreaseBookedCount() throws Exception {
        insertSlotWithBookedCount(10, "2099-01-01", "09:00", 5, 3);

        Optional<AppointmentSlot> before = repository.findById(10);
        assertTrue(before.isPresent());

        boolean updated = repository.decrementBookedCount(10);

        Optional<AppointmentSlot> after = repository.findById(10);
        assertTrue(after.isPresent());

        assertTrue(updated);
        assertEquals(before.get().bookedCount - 1, after.get().bookedCount);
    }

    @Test
    void decrementBookedCount_shouldReturnFalseWhenSlotDoesNotExist() {
        boolean updated = repository.decrementBookedCount(99999);

        assertFalse(updated);
    }

    private void insertSlotWithBookedCount(int id,
                                           String date,
                                           String time,
                                           int capacity,
                                           int bookedCount) throws Exception {
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement()) {

            st.execute("INSERT INTO appointment_slots(id, slot_date, slot_time, capacity, booked_count) " +
                    "VALUES (" + id + ", '" + date + "', '" + time + "', " + capacity + ", " + bookedCount + ")");
        }
    }
}