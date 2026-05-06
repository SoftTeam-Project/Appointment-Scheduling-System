package com.weam.appointments.notification;

import com.weam.appointments.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleNotificationObserverTest {

    private final PrintStream originalOut = System.out;

    @AfterEach
    void restoreSystemOut() {
        System.setOut(originalOut);
    }

    @Test
    void notify_shouldPrintNotificationMessageToConsole() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        ConsoleNotificationObserver observer = new ConsoleNotificationObserver();
        User user = new User("john", "STUDENT", "john@example.com");

        observer.notify(user, "Console reminder");

        String output = outputStream.toString();

        assertTrue(output.contains("john"));
        assertTrue(output.contains("Console reminder"));
    }
}