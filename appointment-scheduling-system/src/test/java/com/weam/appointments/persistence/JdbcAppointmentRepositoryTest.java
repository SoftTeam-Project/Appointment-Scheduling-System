package com.weam.appointments.persistence;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcAppointmentRepositoryTest {

    private JdbcAppointmentRepository repository;

    @BeforeEach
    void setup() {
        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new java.io.File("test-db.sqlite").delete();
        new SchemaInitializer().init();
        repository = new JdbcAppointmentRepository();
    }

    @Test
    void findByIdShouldReturnAppointmentWhenExists() throws Exception {
        int id = insertAppointment(
                1,
                "student",
                "2099-12-20",
                "10:00",
                30,
                1,
                "Confirmed",
                "INDIVIDUAL"
        );

        Optional<Appointment> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals("student", result.get().getUsername());
        assertEquals("2099-12-20", result.get().getDate());
        assertEquals(AppointmentType.INDIVIDUAL, result.get().getType());
    }

    @Test
    void findByIdShouldReturnEmptyWhenMissing() {
        Optional<Appointment> result = repository.findById(99999);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteByIdShouldReturnTrueWhenAppointmentExists() throws Exception {
        int id = insertAppointment(
                1,
                "student",
                "2099-12-21",
                "11:00",
                30,
                1,
                "Confirmed",
                "INDIVIDUAL"
        );

        boolean deleted = repository.deleteById(id);

        assertTrue(deleted);
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void deleteByIdShouldReturnFalseWhenAppointmentMissing() {
        boolean deleted = repository.deleteById(99999);
        assertFalse(deleted);
    }

    @Test
    void findFutureAppointmentsByUserShouldReturnOnlyThatUsersFutureConfirmedAppointments() throws Exception {
        insertAppointment(1, "student", "2099-12-22", "09:00", 30, 1, "Confirmed", "INDIVIDUAL");
        insertAppointment(2, "student", "2099-12-23", "10:00", 45, 2, "Confirmed", "GROUP");
        insertAppointment(3, "admin", "2099-12-24", "11:00", 30, 1, "Confirmed", "INDIVIDUAL");
        insertAppointment(1, "student", "2099-12-25", "12:00", 30, 1, "Cancelled", "INDIVIDUAL");

        List<Appointment> result = repository.findFutureAppointmentsByUser("student");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> a.getUsername().equals("student")));
        assertTrue(result.stream().allMatch(a -> a.getStatus().equals("Confirmed")));
    }

    @Test
    void findAllFutureAppointmentsShouldReturnOnlyConfirmedFutureAppointments() throws Exception {
        insertAppointment(1, "student", "2099-12-26", "09:00", 30, 1, "Confirmed", "INDIVIDUAL");
        insertAppointment(2, "admin", "2099-12-27", "10:00", 60, 2, "Confirmed", "GROUP");
        insertAppointment(3, "student", "2099-12-28", "11:00", 30, 1, "Cancelled", "INDIVIDUAL");

        List<Appointment> result = repository.findAllFutureAppointments();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> a.getStatus().equals("Confirmed")));
    }

    @Test
    void findUpcomingAppointmentsShouldReturnAppointmentsInsideRangeOnly() throws Exception {
        insertAppointment(1, "student", "2099-12-20", "09:00", 30, 1, "Confirmed", "INDIVIDUAL");
        insertAppointment(2, "student", "2099-12-21", "10:00", 30, 1, "Confirmed", "INDIVIDUAL");
        insertAppointment(3, "student", "2099-12-30", "11:00", 30, 1, "Confirmed", "INDIVIDUAL");

        List<Appointment> result = repository.findUpcomingAppointments(
                java.time.LocalDateTime.of(2099, 12, 20, 0, 0),
                java.time.LocalDateTime.of(2099, 12, 22, 0, 0)
        );

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a ->
                a.getDate().equals("2099-12-20") || a.getDate().equals("2099-12-21")));
    }

    @Test
    void saveShouldInsertAppointmentSuccessfully() {
        Appointment appointment = Appointment.builder()
                .id(0)
                .slotId(1)
                .username("student")
                .date("2099-12-29")
                .time("15:00")
                .durationMinutes(30)
                .participants(1)
                .status("Confirmed")
                .type(AppointmentType.INDIVIDUAL)
                .build();

        boolean saved = repository.save(appointment);

        assertTrue(saved);

        List<Appointment> result = repository.findFutureAppointmentsByUser("student");
        assertTrue(result.stream().anyMatch(a ->
                a.getDate().equals("2099-12-29")
                        && a.getTime().equals("15:00")
                        && a.getType() == AppointmentType.INDIVIDUAL));
    }

    private int insertAppointment(int slotId,
                                  String username,
                                  String date,
                                  String time,
                                  int duration,
                                  int participants,
                                  String status,
                                  String type) throws Exception {
        String sql = """
                INSERT INTO appointments
                (slot_id, username, appointment_date, appointment_time, duration_minutes, participants, status, type)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, slotId);
            ps.setString(2, username);
            ps.setString(3, date);
            ps.setString(4, time);
            ps.setInt(5, duration);
            ps.setInt(6, participants);
            ps.setString(7, status);
            ps.setString(8, type);

            int updated = ps.executeUpdate();
            assertEquals(1, updated);

            try (var rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }
}