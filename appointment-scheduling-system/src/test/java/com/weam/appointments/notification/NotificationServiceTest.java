package com.weam.appointments.notification;

import com.weam.appointments.domain.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationServiceTest {

    @Test
    void notifyAllObservers_shouldSendMessageToAllObservers() {
        NotificationService service = new NotificationService();

        FakeObserver firstObserver = new FakeObserver();
        FakeObserver secondObserver = new FakeObserver();

        service.addObserver(firstObserver);
        service.addObserver(secondObserver);

        User user = new User("john", "STUDENT", "john@example.com");

        service.notifyAllObservers(user, "Reminder message");

        assertEquals(1, firstObserver.users.size());
        assertEquals(1, firstObserver.messages.size());
        assertEquals("john", firstObserver.users.get(0).getUsername());
        assertEquals("Reminder message", firstObserver.messages.get(0));

        assertEquals(1, secondObserver.users.size());
        assertEquals(1, secondObserver.messages.size());
        assertEquals("john", secondObserver.users.get(0).getUsername());
        assertEquals("Reminder message", secondObserver.messages.get(0));
    }

    @Test
    void notifyAllObservers_shouldNotNotifyRemovedObserver() {
        NotificationService service = new NotificationService();

        FakeObserver observer = new FakeObserver();
        service.addObserver(observer);
        service.removeObserver(observer);

        User user = new User("jane", "STUDENT", "jane@example.com");

        service.notifyAllObservers(user, "Removed observer message");

        assertEquals(0, observer.users.size());
        assertEquals(0, observer.messages.size());
    }

    @Test
    void notifyAllObservers_shouldNotThrowWhenThereAreNoObservers() {
        NotificationService service = new NotificationService();

        User user = new User("bob", "STUDENT", "bob@example.com");

        assertDoesNotThrow(() ->
                service.notifyAllObservers(user, "No observers message")
        );
    }

    private static class FakeObserver implements NotificationObserver {
        private final List<User> users = new ArrayList<>();
        private final List<String> messages = new ArrayList<>();

        @Override
        public void notify(User user, String message) {
            users.add(user);
            messages.add(message);
        }
    }
}