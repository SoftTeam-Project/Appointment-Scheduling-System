package com.weam.appointments.service;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentType;
import com.weam.appointments.domain.User;
import com.weam.appointments.notification.NotificationService;
import com.weam.appointments.persistence.AppointmentRepository;
import com.weam.appointments.persistence.UserRecord;
import com.weam.appointments.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    private FakeNotificationService notificationService;

    private Clock fixedClock;
    private ReminderService reminderService;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(
                Instant.parse("2026-03-01T09:00:00Z"),
                ZoneId.systemDefault()
        );

        notificationService = new FakeNotificationService();

        reminderService = new ReminderService(
                appointmentRepository,
                userRepository,
                notificationService,
                fixedClock
        );
    }

    @Test
    void sendReminders_shouldSendOnlyForUpcomingAppointments() {
        Appointment upcoming1 = Appointment.builder()
                .id(1)
                .slotId(1)
                .username("john")
                .date("2026-03-01")
                .time("10:00")
                .durationMinutes(30)
                .participants(2)
                .status("Confirmed")
                .type(AppointmentType.GROUP)
                .build();

        Appointment upcoming2 = Appointment.builder()
                .id(2)
                .slotId(2)
                .username("jane")
                .date("2026-03-01")
                .time("14:00")
                .durationMinutes(60)
                .participants(1)
                .status("Confirmed")
                .type(AppointmentType.INDIVIDUAL)
                .build();

        when(appointmentRepository.findUpcomingAppointments(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of(upcoming1, upcoming2));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(
                        new UserRecord("john", "pass", "STUDENT", "john@example.com")
                ));

        when(userRepository.findByUsername("jane"))
                .thenReturn(Optional.of(
                        new UserRecord("jane", "pass", "STUDENT", "jane@example.com")
                ));

        reminderService.sendReminders();

        assertEquals(2, notificationService.getUsers().size());
        assertEquals(2, notificationService.getMessages().size());

        assertEquals("john", notificationService.getUsers().get(0).getUsername());
        assertEquals(
                "Reminder: You have an appointment on 2026-03-01 at 10:00.",
                notificationService.getMessages().get(0)
        );

        assertEquals("jane", notificationService.getUsers().get(1).getUsername());
        assertEquals(
                "Reminder: You have an appointment on 2026-03-01 at 14:00.",
                notificationService.getMessages().get(1)
        );
    }

    @Test
    void sendReminders_shouldNotSendIfNoUpcoming() {
        when(appointmentRepository.findUpcomingAppointments(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        reminderService.sendReminders();

        assertEquals(0, notificationService.getUsers().size());
        assertEquals(0, notificationService.getMessages().size());
    }

    private static class FakeNotificationService extends NotificationService {
        private final List<User> users = new ArrayList<>();
        private final List<String> messages = new ArrayList<>();

        @Override
        public void notifyAllObservers(User user, String message) {
            users.add(user);
            messages.add(message);
        }

        List<User> getUsers() {
            return users;
        }

        List<String> getMessages() {
            return messages;
        }
    }
}