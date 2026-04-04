package com.weam.appointments.notification;

import com.weam.appointments.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.mail.Transport;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationObserverTest {

    @Test
    void notify_shouldSendEmail() throws Exception {
        try (MockedStatic<Transport> transportMock = Mockito.mockStatic(Transport.class)) {
            EmailNotificationObserver observer = new EmailNotificationObserver();
            // ✅ التعديل هنا: إضافة username و email
            User user = new User("testuser", "STUDENT", "test@example.com");

            observer.notify(user, "Test message");

            transportMock.verify(() -> Transport.send(any(jakarta.mail.Message.class)), times(1));
        }
    }
}