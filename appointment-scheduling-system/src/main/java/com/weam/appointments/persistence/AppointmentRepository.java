package com.weam.appointments.persistence;

import com.weam.appointments.domain.Appointment;

public interface AppointmentRepository {
    boolean save(Appointment appointment);
}