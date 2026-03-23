package com.weam.appointments.notification;

import com.weam.appointments.domain.User;
public interface NotificationObserver {

	
	void notify(User user, String message);
}
