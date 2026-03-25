package com.weam.appointments.persistence;

import com.weam.appointments.domain.Appointment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    boolean save(Appointment appointment);
    Optional<Appointment> findById(int id);
    boolean deleteById(int id);
    List<Appointment> findAllFutureAppointments();
    List<Appointment> findUpcomingAppointments(LocalDateTime from, LocalDateTime to);
    List<Appointment> findFutureAppointmentsByUser(String username);
}