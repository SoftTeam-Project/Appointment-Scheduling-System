package com.weam.appointments.persistence;

import com.weam.appointments.domain.AppointmentSlot;
import java.sql.*;
import java.util.*;

public class JdbcSlotRepository implements SlotRepository {

    @Override
    public List<AppointmentSlot> findAvailableSlots() {
        String sql = """
            SELECT id, slot_date, slot_time, capacity, booked_count
            FROM appointment_slots
            WHERE booked_count < capacity
            ORDER BY slot_date, slot_time
        """;
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<AppointmentSlot> slots = new ArrayList<>();
            while (rs.next()) {
                slots.add(new AppointmentSlot(
                        rs.getInt("id"),
                        rs.getString("slot_date"),
                        rs.getString("slot_time"),
                        rs.getInt("capacity"),
                        rs.getInt("booked_count")
                ));
            }
            return slots;

        } catch (Exception e) {
            throw new RuntimeException("DB query failed", e);
        }
    }

    @Override
    public Optional<AppointmentSlot> findById(int slotId) {
        String sql = "SELECT id, slot_date, slot_time, capacity, booked_count FROM appointment_slots WHERE id = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new AppointmentSlot(
                        rs.getInt("id"),
                        rs.getString("slot_date"),
                        rs.getString("slot_time"),
                        rs.getInt("capacity"),
                        rs.getInt("booked_count")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("DB query failed", e);
        }
        return Optional.empty();
    }
    @Override
    public boolean addSlot(String date, String time, int capacity) {
        String sql = "INSERT INTO appointment_slots(slot_date, slot_time, capacity, booked_count) VALUES (?, ?, ?, 0)";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, date);
            ps.setString(2, time);
            ps.setInt(3, capacity);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean incrementBookedCount(int slotId) {
        String sql = "UPDATE appointment_slots SET booked_count = booked_count + 1 WHERE id = ?";
        return updateBookedCount(slotId, sql);
    }

    @Override
    public boolean decrementBookedCount(int slotId) {
        String sql = "UPDATE appointment_slots SET booked_count = booked_count - 1 WHERE id = ?";
        return updateBookedCount(slotId, sql);
    }

    private boolean updateBookedCount(int slotId, String sql) {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}