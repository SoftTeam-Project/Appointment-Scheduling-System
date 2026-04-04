package com.weam.appointments.service;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.User;
import com.weam.appointments.notification.NotificationService;
import com.weam.appointments.persistence.AppointmentRepository;
import com.weam.appointments.persistence.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public class ReminderService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    
    public ReminderService(AppointmentRepository appointmentRepository, UserRepository userRepository, NotificationService notificationService, Clock clock) {
        
    	this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

   
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime tomorrow = now.plus(24, ChronoUnit.HOURS);

        var appointments = appointmentRepository.findUpcomingAppointments(now, tomorrow);

        for (Appointment appointment : appointments) {
        	System.out.println("Sending reminder for appointment ID: " + appointment.getId());
            userRepository.findByUsername(appointment.getUsername())
                .ifPresent(userRecord -> {
                	
                    User user = new User(userRecord.username(), userRecord.role(),userRecord.email());
                    String message = String.format(
                        "Reminder: You have an appointment on %s at %s.",
                        appointment.getDate(), appointment.getTime()
                    );
                    notificationService.notifyAllObservers(user, message);
                });
        }
    }
}