package com.weam.appointments.presentation;

import com.weam.appointments.notification.NotificationService;
import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CliAppAdditionalCoverageTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Path dbPath = tempDir.resolve("cli-additional-test.sqlite");
        System.setProperty("db.url", "jdbc:sqlite:" + dbPath);
        new SchemaInitializer().init();
    }

    @Test
    void runOnce_shouldExitImmediatelyWhenUserTypesExit() {
        String input = "exit\n";
        String output = runCli(input);

        assertTrue(output.contains("Appointment Scheduling System"));
        assertTrue(output.contains("Username"));
    }

    @Test
    void runOnce_shouldPrintInvalidCredentialsForWrongLogin() {
        String input = """
                wrongUser
                wrongPassword
                """;

        String output = runCli(input);

        assertTrue(output.contains("Invalid credentials"));
    }

    @Test
    void runOnce_shouldHandleInvalidAppointmentTypeThenLogout() {
        String input = """
                student
                stud123
                2
                1
                30
                1
                9
                4
                """;

        String output = runCli(input);

        assertTrue(output.contains("Login successful"));
        assertTrue(output.contains("Invalid appointment type."));
        assertTrue(output.contains("Logged out."));
    }

    @Test
    void runOnce_shouldHandleStudentTryingAdminOptionThenLogout() {
        String input = """
                student
                stud123
                6
                4
                """;

        String output = runCli(input);

        assertTrue(output.contains("Login successful"));
        assertTrue(output.contains("Invalid choice."));
        assertTrue(output.contains("Logged out."));
    }

    @Test
    void runOnce_shouldSendRemindersThenLogout() {
        String input = """
                student
                stud123
                3
                4
                """;

        String output = runCli(input);

        assertTrue(output.contains("Login successful"));
        assertTrue(output.contains("Reminders sent."));
        assertTrue(output.contains("Logged out."));
    }

    @Test
    void runOnce_shouldShowAdminCancelMenuThenLogout() {
        String input = """
                admin
                admin123
                6
                4
                """;

        String output = runCli(input);

        assertTrue(output.contains("Login successful"));
        assertTrue(output.contains("Admin: Cancel any appointment"));
        assertTrue(
                output.contains("No upcoming appointments.")
                        || output.contains("All upcoming appointments:")
        );
    }

    private String runCli(String input) {
        AuthService auth = new AuthService(new JdbcUserRepository());
        NotificationService notificationService = new NotificationService();

        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outContent);

        CliApp app = new CliApp(auth, notificationService, in, out);
        app.runOnce();

        return outContent.toString();
    }
}