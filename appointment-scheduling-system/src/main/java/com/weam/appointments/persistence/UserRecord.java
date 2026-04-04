package com.weam.appointments.persistence;

public record UserRecord(String username, String password, String role, String email) {}