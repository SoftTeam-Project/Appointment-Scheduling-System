package com.weam.appointments.presentation.gui;

import com.weam.appointments.domain.AppointmentSlot;
import com.weam.appointments.persistence.JdbcSlotRepository;
import com.weam.appointments.service.SlotService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AvailableSlotsFrame {

    public AvailableSlotsFrame(JFrame previousFrame) {
        JFrame frame = new JFrame("Available Slots");

        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);

        UITheme.GradientPanel background = new UITheme.GradientPanel();
        background.setLayout(new BorderLayout(20, 20));
        background.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        frame.setContentPane(background);

        JButton backBtn = new JButton("Back");

        backBtn.addActionListener(e -> {
            frame.dispose();

            if (previousFrame != null) {
                previousFrame.setVisible(true);
            }
        });

        String[] columns = {"ID", "Date", "Time", "Capacity", "Booked"};

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        SlotService slotService = new SlotService(new JdbcSlotRepository());
        List<AppointmentSlot> slots = slotService.viewAvailableSlots();

        for (AppointmentSlot slot : slots) {
            model.addRow(new Object[]{
                    slot.id,
                    slot.date,
                    slot.time,
                    slot.capacity,
                    slot.bookedCount
            });
        }

        JScrollPane scrollPane = new JScrollPane(table);

        background.add(backBtn, BorderLayout.NORTH);
        background.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}