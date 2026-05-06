package com.weam.appointments.presentation;

import com.weam.appointments.notification.NotificationService;
import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("cliScenarios")
    void runOnce_shouldHandleCommonScenarios(String input, List<String> expectedOutputs) {
        String output = runCli(input);

        for (String expected : expectedOutputs) {
            assertTrue(output.contains(expected));
        }
    }

    static Stream<Arguments> cliScenarios() {
        return Stream.of(
                Arguments.of(
                        "exit\n",
                        List.of("Appointment Scheduling System", "Username")
                ),
                Arguments.of(
                        """
                        wrongUser
                        wrongPassword
                        """,
                        List.of("Invalid credentials")
                ),
                Arguments.of(
                        """
                        student
                        stud123
                        2
                        1
                        30
                        1
                        9
                        4
                        """,
                        List.of("Login successful", "Invalid appointment type.", "Logged out.")
                ),
                Arguments.of(
                        """
                        student
                        stud123
                        6
                        4
                        """,
                        List.of("Login successful", "Invalid choice.", "Logged out.")
                ),
                Arguments.of(
                        """
                        student
                        stud123
                        3
                        4
                        """,
                        List.of("Login successful", "Reminders sent.", "Logged out.")
                )
        );
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