package com.weam.appointments.presentation;

import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

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
        String input = "admin\nadmin123\n2\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBytes);

        AuthService auth = new AuthService(new JdbcUserRepository());
        CliApp app = new CliApp(auth, in, out);

        app.runOnce();

        String output = outBytes.toString();
        System.out.println(output);

        assertTrue(output.contains("1) View available slots"),
                "Should show menu after successful login");
        assertTrue(output.contains("2) Logout"),
                "Should show logout option");
        assertTrue(output.toLowerCase().contains("logged out"),
                "Should logout when choosing option 2");
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
}