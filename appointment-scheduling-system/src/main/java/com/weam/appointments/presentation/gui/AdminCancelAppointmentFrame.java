package com.weam.appointments.presentation.gui;

import com.weam.appointments.persistence.JdbcAppointmentRepository;
import com.weam.appointments.persistence.JdbcSlotRepository;
import com.weam.appointments.service.BookingService;

import javax.swing.*;
import java.awt.*;

public class AdminCancelAppointmentFrame {

    public AdminCancelAppointmentFrame(JFrame previousFrame) {
        JFrame frame = new JFrame("Admin Cancel Appointment");

        frame.setSize(1000, 700);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);

        UITheme.GradientPanel background = new UITheme.GradientPanel();
        background.setLayout(new GridBagLayout());
        frame.setContentPane(background);

        JPanel card = new JPanel();
        card.setLayout(null);
        card.setPreferredSize(new Dimension(420, 260));
        card.setBackground(new Color(255, 255, 255, 230));

        JLabel idLabel = new JLabel("Appointment ID:");
        idLabel.setBounds(40, 50, 140, 30);

        JTextField idField = new JTextField();
        idField.setBounds(180, 50, 160, 30);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBounds(70, 150, 120, 40);

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(220, 150, 120, 40);

        BookingService bookingService = new BookingService(
                new JdbcAppointmentRepository(),
                new JdbcSlotRepository()
        );

        cancelBtn.addActionListener(e -> {
            try {
                int appointmentId = Integer.parseInt(idField.getText());

                boolean cancelled = bookingService.adminCancelAppointment(appointmentId);

                if (cancelled) {
                    JOptionPane.showMessageDialog(frame, "Appointment cancelled successfully");
                    frame.dispose();

                    if (previousFrame != null) {
                        previousFrame.setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Cancel failed. Check appointment ID.");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Appointment ID must be a number");
            }
        });

        backBtn.addActionListener(e -> {
            frame.dispose();

            if (previousFrame != null) {
                previousFrame.setVisible(true);
            }
        });

        card.add(idLabel);
        card.add(idField);
        card.add(cancelBtn);
        card.add(backBtn);

        background.add(card);

        frame.setVisible(true);
    }
}