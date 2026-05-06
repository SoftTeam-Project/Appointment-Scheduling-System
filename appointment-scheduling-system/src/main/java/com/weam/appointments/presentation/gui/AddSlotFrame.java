package com.weam.appointments.presentation.gui;

import com.weam.appointments.persistence.JdbcSlotRepository;
import com.weam.appointments.service.SlotService;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.*;

public class AddSlotFrame {

    public AddSlotFrame(JFrame previousFrame) {

        JFrame frame = new JFrame("Add Slot");

        frame.setSize(1000, 700);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);

        UITheme.GradientPanel background = new UITheme.GradientPanel();
        background.setLayout(new GridBagLayout());
        frame.setContentPane(background);

        JPanel card = new JPanel();
        card.setLayout(null);
        card.setPreferredSize(new Dimension(430, 330));
        card.setBackground(new java.awt.Color(255, 255, 255, 230));

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setBounds(50, 40, 100, 25);

        JTextField dateField = new JTextField("2026-03-05");
        dateField.setBounds(160, 40, 200, 30);

        JLabel timeLabel = new JLabel("Time:");
        timeLabel.setBounds(50, 90, 100, 25);

        JTextField timeField = new JTextField("10:00");
        timeField.setBounds(160, 90, 200, 30);

        JLabel capacityLabel = new JLabel("Capacity:");
        capacityLabel.setBounds(50, 140, 100, 25);

        JTextField capacityField = new JTextField();
        capacityField.setBounds(160, 140, 200, 30);

        JButton addBtn = new JButton("Add");
        addBtn.setBounds(95, 220, 110, 40);

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(225, 220, 110, 40);

        SlotService slotService = new SlotService(new JdbcSlotRepository());

        addBtn.addActionListener(e -> {
            String date = dateField.getText();
            String time = timeField.getText();

            try {
                int capacity = Integer.parseInt(capacityField.getText());

                boolean added = slotService.addSlot(date, time, capacity);

                if (added) {
                    JOptionPane.showMessageDialog(frame, "Slot added successfully");
                    frame.dispose();

                    if (previousFrame != null) {
                        previousFrame.setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to add slot");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Capacity must be a number");
            }
        });

        backBtn.addActionListener(e -> {
            frame.dispose();

            if (previousFrame != null) {
                previousFrame.setVisible(true);
            }
        });

        card.add(dateLabel);
        card.add(dateField);
        card.add(timeLabel);
        card.add(timeField);
        card.add(capacityLabel);
        card.add(capacityField);
        card.add(addBtn);
        card.add(backBtn);

        background.add(card);

        frame.setVisible(true);
    }
}