package com.weam.appointments.persistence;

import java.time.LocalDateTime;
import java.util.List;
import com.weam.appointments.domain.Appointment;

public interface AppointmentRepository {
    boolean save(Appointment appointment);
    
    List<Appointment> findUpcomingAppointments(LocalDateTime from, LocalDateTime to);
}