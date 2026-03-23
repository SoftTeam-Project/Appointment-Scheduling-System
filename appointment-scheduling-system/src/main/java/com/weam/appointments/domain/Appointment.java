package com.weam.appointments.domain;

public class Appointment {
    private final int id;
    private final int slotId;
    private final String username;
    private final String date;
    private final String time;
    private final int durationMinutes;
    private final int participants;
    private final String status;

    public Appointment(int id, int slotId, String username,String date, String time, int durationMinutes, int participants, String status) {
        this.id = id;
        this.slotId = slotId;
        this.username = username;
        this.date = date;
        this.time = time;
        this.durationMinutes = durationMinutes;
        this.participants = participants;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getSlotId() {
        return slotId;
    }

    public String getUsername() {
        return username;
    }
    public String getDate() {
    	return date;
    	
   	}
    public String getTime() {
    	return time;
   	}

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getParticipants() {
        return participants;
    }

    public String getStatus() {
        return status;
    }
}