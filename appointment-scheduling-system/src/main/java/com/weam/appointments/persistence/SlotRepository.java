package com.weam.appointments.persistence;

import com.weam.appointments.domain.AppointmentSlot;
import java.util.List;
import java.util.Optional;

public interface SlotRepository {
    List<AppointmentSlot> findAvailableSlots();
    Optional<AppointmentSlot> findById(int slotId);
    boolean incrementBookedCount(int slotId);
    boolean decrementBookedCount(int slotId);
    boolean addSlot(String date, String time, int capacity);
}