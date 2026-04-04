package com.weam.appointments.presentation;

import com.weam.appointments.notification.NotificationService;
import com.weam.appointments.persistence.Db;
import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class CliAppTest {

    private NotificationService notificationService;

    @BeforeEach
    void setup() {
        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new java.io.File("test-db.sqlite").delete();
        new SchemaInitializer().init();
        notificationService = new NotificationService();
    }

    @Test
    void exitShouldFinishImmediately() {
        String input = "exit\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Username"), "Should prompt for username");
        assertTrue(output.contains("exit"), "Prompt should mention exit");
    }

    @Test
    void validLoginShouldShowSuccessThenLogout() {
        String input = "admin\nadmin123\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();

        assertTrue(output.contains("1) View available slots"),
                "Should show view slots option");
        assertTrue(output.contains("2) Book appointment"),
                "Should show book appointment option");
        assertTrue(output.contains("3) Send reminders"),
                "Should show send reminders option");
        assertTrue(output.contains("4) Logout"),
                "Should show logout option");
        assertTrue(output.toLowerCase().contains("logged out"),
                "Should logout when choosing option 4");
    }

    @Test
    void invalidLoginPrintsError() {
        String input = "admin\nwrong\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.toLowerCase().contains("invalid"),
                "Should print invalid credentials message");
    }

    @Test
    void viewAvailableSlotsShouldPrintSlots() {
        String input = "admin\nadmin123\n1\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Available slots:"),
                "Should print available slots header");
        assertTrue(output.contains("2026-03-01"),
                "Should print seeded slot date");
    }

    @Test
    void validBookingShouldPrintSuccess() {
        String input = "admin\nadmin123\n2\n1\n60\n2\n7\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Appointment booked successfully."),
                "Should print success message after valid booking");
    }

    @Test
    void invalidBookingShouldPrintFailure() {
        String input = "admin\nadmin123\n2\n1\n180\n2\n1\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Booking failed."),
                "Should print failure message for invalid booking");
    }

    @Test
    void invalidMenuChoiceShouldPrintInvalidChoice() {
        String input = "admin\nadmin123\n9\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Invalid choice."),
                "Should print invalid choice for unsupported menu option");
    }

    @Test
    void invalidAppointmentTypeShouldPrintInvalidAppointmentType() {
        String input = "admin\nadmin123\n2\n1\n60\n2\n9\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Invalid appointment type."),
                "Should print invalid appointment type message");
    }

    @Test
    void nonAdminChoosingAdminCancelShouldPrintInvalidChoice() {
        String input = "student\nstud123\n6\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Invalid choice."),
                "Student should not be allowed to use admin cancel option");
    }

    @Test
    void sendRemindersShouldPrintConfirmation() {
        String input = "admin\nadmin123\n3\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Reminders sent."),
                "Should print confirmation after sending reminders");
    }

    @Test
    void cancelAppointmentWhenNoUpcomingAppointmentsShouldPrintMessage() {
        String input = "student\nstud123\n5\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("You have no upcoming appointments."),
                "Should inform user when there are no appointments to cancel");
    }

    @Test
    void adminCancelWhenNoUpcomingAppointmentsShouldPrintMessage() {
        String input = "admin\nadmin123\n6\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("No upcoming appointments."),
                "Admin should see message when there are no future appointments");
    }

    @Test
    void viewAvailableSlotsWhenEmptyShouldPrintNoAvailableSlots() throws Exception {
        deleteAllSlots();

        String input = "admin\nadmin123\n1\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("No available slots."),
                "Should print no available slots when table is empty");
    }

    @Test
    void cancelAppointmentShouldSucceedForFutureOwnedAppointment() throws Exception {
        int id = insertFutureAppointment("student");

        String input = "student\nstud123\n5\n" + id + "\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Your upcoming appointments:"));
        assertTrue(output.contains("Appointment cancelled successfully."));
    }

    @Test
    void cancelAppointmentShouldFailForInvalidId() throws Exception {
        insertFutureAppointment("student");

        String input = "student\nstud123\n5\n999\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Your upcoming appointments:"));
        assertTrue(output.contains("Cancellation failed. Ensure the appointment is in the future and belongs to you."));
    }

    @Test
    void adminCancelShouldSucceedForFutureAppointment() throws Exception {
        int id = insertFutureAppointment("student");

        String input = "admin\nadmin123\n6\n" + id + "\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("All upcoming appointments:"));
        assertTrue(output.contains("Appointment cancelled successfully by admin."));
    }

    @Test
    void adminCancelShouldFailForInvalidId() throws Exception {
        insertFutureAppointment("student");

        String input = "admin\nadmin123\n6\n999\n4\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, notificationService, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("All upcoming appointments:"));
        assertTrue(output.contains("Admin cancellation failed."));
    }

    private int insertFutureAppointment(String username) throws Exception {
        String sql = """
            INSERT INTO appointments
            (slot_id, username, appointment_date, appointment_time, duration_minutes, participants, status, type)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (var con = Db.getConnection();
             var ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, 1);
            ps.setString(2, username);
            ps.setString(3, "2099-12-25");
            ps.setString(4, "10:00");
            ps.setInt(5, 30);
            ps.setInt(6, 1);
            ps.setString(7, "Confirmed");
            ps.setString(8, "INDIVIDUAL");
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private void deleteAllSlots() throws Exception {
        try (var con = Db.getConnection();
             var st = con.createStatement()) {
            st.executeUpdate("DELETE FROM appointment_slots");
        }
    }
}