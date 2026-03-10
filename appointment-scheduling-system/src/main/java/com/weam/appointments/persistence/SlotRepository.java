package com.weam.appointments.persistence;

import com.weam.appointments.domain.AppointmentSlot;
import java.util.List;

public interface SlotRepository {
    List<AppointmentSlot> findAvailableSlots();
}