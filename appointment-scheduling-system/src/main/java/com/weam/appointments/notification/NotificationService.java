package com.weam.appointments.notification;

import com.weam.appointments.domain.User;
import java.util.ArrayList;
import java.util.List;


public class NotificationService {
    private final List<NotificationObserver> observers = new ArrayList<>();

    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

   
    public void notifyAllObservers(User user, String message) {
        for (NotificationObserver observer : observers) {
            observer.notify(user, message);
        }
    }
}