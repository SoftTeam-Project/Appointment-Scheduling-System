package com.weam.appointments.presentation;

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

    @BeforeEach
    void setup() {
        System.setProperty("db.url", "jdbc:sqlite:test-db.sqlite");
        new java.io.File("test-db.sqlite").delete();
        new SchemaInitializer().init();
    }

    @Test
    void exitShouldFinishImmediately() {
        String input = "exit\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Username"), "Should prompt for username");
        assertTrue(output.contains("exit"), "Prompt should mention exit");
    }

    @Test
    void validLoginShouldShowSuccessThenLogout() {
        String input = "admin\nadmin123\n3\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, in, out);

        app.runOnce();

        String output = outBytes.toString();

        assertTrue(output.contains("1) View available slots"),
                "Should show view slots option");
        assertTrue(output.contains("2) Book appointment"),
                "Should show book appointment option");
        assertTrue(output.contains("3) Logout"),
                "Should show logout option");
        assertTrue(output.toLowerCase().contains("logged out"),
                "Should logout when choosing option 3");
    }

    @Test
    void invalidLoginPrintsError() {
        String input = "admin\nwrong\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.toLowerCase().contains("invalid"),
                "Should print invalid credentials message");
    }

    @Test
    void viewAvailableSlotsShouldPrintSlots() {
        String input = "admin\nadmin123\n1\n3\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Available slots:"),
                "Should print available slots header");
        assertTrue(output.contains("2026-03-01"),
                "Should print seeded slot date");
    }

    @Test
    void validBookingShouldPrintSuccess() {
        String input = "admin\nadmin123\n2\n1\n60\n2\n3\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Appointment booked successfully"),
                "Should print success message after valid booking");
    }

    @Test
    void invalidBookingShouldPrintFailure() {
        String input = "admin\nadmin123\n2\n1\n180\n2\n3\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, in, out);

        app.runOnce();

        String output = outBytes.toString();
        assertTrue(output.contains("Booking failed"),
                "Should print failure message for invalid booking");
    }
}