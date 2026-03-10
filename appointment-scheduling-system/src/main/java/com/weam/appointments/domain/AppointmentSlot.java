package com.weam.appointments.domain;

public class AppointmentSlot {
    public final int id;
    public final String date;
    public final String time;
    public final int capacity;
    public final int bookedCount;

    public AppointmentSlot(int id, String date, String time, int capacity, int bookedCount) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.capacity = capacity;
        this.bookedCount = bookedCount;
    }

    public boolean isAvailable() {
        return bookedCount < capacity;
    }
}