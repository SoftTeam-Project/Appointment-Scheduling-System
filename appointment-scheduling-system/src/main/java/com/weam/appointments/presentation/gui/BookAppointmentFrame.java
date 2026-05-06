package com.weam.appointments.presentation.gui;

import com.weam.appointments.domain.AppointmentType;
import com.weam.appointments.persistence.JdbcAppointmentRepository;
import com.weam.appointments.persistence.JdbcSlotRepository;
import com.weam.appointments.service.BookingService;

import javax.swing.*;
import java.awt.*;

public class BookAppointmentFrame {

    public BookAppointmentFrame(String username, JFrame previousFrame) {

        JFrame frame = new JFrame("Book Appointment");

        frame.setSize(1000, 700);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);

        UITheme.GradientPanel background = new UITheme.GradientPanel();
        background.setLayout(new GridBagLayout());
        frame.setContentPane(background);

        JPanel card = new JPanel();
        card.setLayout(null);
        card.setPreferredSize(new Dimension(430, 360));
        card.setBackground(new Color(255, 255, 255, 230));

        JLabel slotIdLabel = new JLabel("Slot ID:");
        slotIdLabel.setBounds(50, 40, 120, 25);

        JTextField slotIdField = new JTextField();
        slotIdField.setBounds(180, 40, 170, 30);

        JLabel durationLabel = new JLabel("Duration:");
        durationLabel.setBounds(50, 90, 120, 25);

        JTextField durationField = new JTextField("30");
        durationField.setBounds(180, 90, 170, 30);

        JLabel participantsLabel = new JLabel("Participants:");
        participantsLabel.setBounds(50, 140, 120, 25);

        JTextField participantsField = new JTextField("1");
        participantsField.setBounds(180, 140, 170, 30);

        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setBounds(50, 190, 120, 25);

        JComboBox<AppointmentType> typeBox = new JComboBox<>(AppointmentType.values());
        typeBox.setBounds(180, 190, 170, 30);

        JButton bookBtn = new JButton("Book");
        bookBtn.setBounds(85, 270, 110, 40);

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(225, 270, 110, 40);

        BookingService bookingService = new BookingService(
                new JdbcAppointmentRepository(),
                new JdbcSlotRepository()
        );

        bookBtn.addActionListener(e -> {
            try {
                int slotId = Integer.parseInt(slotIdField.getText());
                int duration = Integer.parseInt(durationField.getText());
                int participants = Integer.parseInt(participantsField.getText());
                AppointmentType type = (AppointmentType) typeBox.getSelectedItem();

                boolean booked = bookingService.bookAppointment(
                        slotId,
                        username,
                        duration,
                        participants,
                        type
                );

                if (booked) {
                    JOptionPane.showMessageDialog(frame, "Appointment booked successfully");
                    frame.dispose();

                    if (previousFrame != null) {
                        previousFrame.setVisible(true);
                    }

                } else {
                    JOptionPane.showMessageDialog(frame, "Booking failed. Check slot, duration, participants, or type.");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Slot ID, Duration, and Participants must be numbers");
            }
        });

        backBtn.addActionListener(e -> {
            frame.dispose();

            if (previousFrame != null) {
                previousFrame.setVisible(true);
            }
        });

        card.add(slotIdLabel);
        card.add(slotIdField);
        card.add(durationLabel);
        card.add(durationField);
        card.add(participantsLabel);
        card.add(participantsField);
        card.add(typeLabel);
        card.add(typeBox);
        card.add(bookBtn);
        card.add(backBtn);

        background.add(card);

        frame.setVisible(true);
    }
}