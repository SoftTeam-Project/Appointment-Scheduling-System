package com.weam.appointments.service;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentSlot;
import com.weam.appointments.persistence.AppointmentRepository;
import com.weam.appointments.persistence.SlotRepository;

import java.util.List;

public class BookingService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;

    public BookingService(AppointmentRepository appointmentRepository, SlotRepository slotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
    }

    public boolean isValidDuration(int durationMinutes) {
        return durationMinutes > 0 && durationMinutes <= 120;
    }

    public boolean isValidParticipants(int participants) {
        return participants > 0 && participants <= 5;
    }

    public boolean isSlotAvailable(int slotId) {
        List<AppointmentSlot> slots = slotRepository.findAvailableSlots();
        return slots.stream().anyMatch(slot -> slot.id == slotId);
    }

    public boolean bookAppointment(int slotId, String username, int durationMinutes, int participants) {
        if (!isValidDuration(durationMinutes)) {
            return false;
        }

        if (!isValidParticipants(participants)) {
            return false;
        }

        if (!isSlotAvailable(slotId)) {
            return false;
        }

        Appointment appointment = new Appointment(
                0,
                slotId,
                username,
                durationMinutes,
                participants,
                "Confirmed"
        );

        return appointmentRepository.save(appointment);
    }
}