package com.weam.appointments.notification;

import com.weam.appointments.domain.User;


public class ConsoleNotificationObserver implements NotificationObserver {
    @Override
    public void notify(User user, String message) {
        System.out.println("🔔 Notification for " + user.getUsername() + ": " + message);
    }
}