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
    private final AppointmentType type;

    private Appointment(Builder builder) {
        this.id = builder.id;
        this.slotId = builder.slotId;
        this.username = builder.username;
        this.date = builder.date;
        this.time = builder.time;
        this.durationMinutes = builder.durationMinutes;
        this.participants = builder.participants;
        this.status = builder.status;
        this.type = builder.type;
    }

    public static Builder builder() {
        return new Builder();
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

    public AppointmentType getType() {
        return type;
    }

    public static class Builder {
        private int id;
        private int slotId;
        private String username;
        private String date;
        private String time;
        private int durationMinutes;
        private int participants;
        private String status;
        private AppointmentType type = AppointmentType.INDIVIDUAL;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder slotId(int slotId) {
            this.slotId = slotId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder date(String date) {
            this.date = date;
            return this;
        }

        public Builder time(String time) {
            this.time = time;
            return this;
        }

        public Builder durationMinutes(int durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public Builder participants(int participants) {
            this.participants = participants;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder type(AppointmentType type) {
            this.type = type;
            return this;
        }

        public Appointment build() {
            return new Appointment(this);
        }
    }
}