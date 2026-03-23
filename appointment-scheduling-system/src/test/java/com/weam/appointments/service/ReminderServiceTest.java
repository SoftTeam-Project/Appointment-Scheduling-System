package com.weam.appointments.service;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.User;
import com.weam.appointments.notification.NotificationService;
import com.weam.appointments.persistence.AppointmentRepository;
import com.weam.appointments.persistence.UserRecord;
import com.weam.appointments.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Captor private ArgumentCaptor<User> userCaptor;
    @Captor private ArgumentCaptor<String> messageCaptor;

    private Clock fixedClock;
    private ReminderService reminderService;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-03-01T09:00:00Z"), ZoneId.systemDefault());
        reminderService = new ReminderService(
            appointmentRepository,
            userRepository,
            notificationService,
            fixedClock
        );
    }

    @Test
    void sendReminders_shouldSendOnlyForUpcomingAppointments() {
        Appointment upcoming1 = new Appointment(1, 1, "john", "2026-03-01", "10:00", 30, 2, "Confirmed");
        Appointment upcoming2 = new Appointment(2, 2, "jane", "2026-03-01", "14:00", 60, 1, "Confirmed");
        Appointment past = new Appointment(3, 3, "bob", "2026-02-28", "15:00", 30, 1, "Confirmed");

        LocalDateTime now = LocalDateTime.now(fixedClock);
        LocalDateTime tomorrow = now.plusHours(24);

        when(appointmentRepository.findUpcomingAppointments(now, tomorrow))
            .thenReturn(List.of(upcoming1, upcoming2));

        // ✅ التعديل هنا: إضافة email كمعامل رابع
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new UserRecord("john", "pass", "STUDENT", "john@example.com")));
        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(new UserRecord("jane", "pass", "STUDENT", "jane@example.com")));

        reminderService.sendReminders();

        verify(notificationService, times(2)).notifyAllObservers(userCaptor.capture(), messageCaptor.capture());

        List<User> users = userCaptor.getAllValues();
        List<String> messages = messageCaptor.getAllValues();

        assertEquals("john", users.get(0).getUsername());
        assertEquals("Reminder: You have an appointment on 2026-03-01 at 10:00.", messages.get(0));
        assertEquals("jane", users.get(1).getUsername());
        assertEquals("Reminder: You have an appointment on 2026-03-01 at 14:00.", messages.get(1));
    }

    @Test
    void sendReminders_shouldNotSendIfNoUpcoming() {
        LocalDateTime now = LocalDateTime.now(fixedClock);
        LocalDateTime tomorrow = now.plusHours(24);
        
        when(appointmentRepository.findUpcomingAppointments(now, tomorrow)).thenReturn(List.of());

        reminderService.sendReminders();

        verify(notificationService, never()).notifyAllObservers(any(), any());
    }
}