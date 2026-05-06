package com.weam.appointments.service;

import com.weam.appointments.domain.AppointmentSlot;
import com.weam.appointments.persistence.SlotRepository;
import java.util.List;

public class SlotService {
    private final SlotRepository repo;

    public SlotService(SlotRepository repo) {
        this.repo = repo;
    }

    public List<AppointmentSlot> viewAvailableSlots() {
        return repo.findAvailableSlots();
    }
    public boolean addSlot(String date, String time, int capacity) {
        return repo.addSlot(date, time, capacity);
    }
}