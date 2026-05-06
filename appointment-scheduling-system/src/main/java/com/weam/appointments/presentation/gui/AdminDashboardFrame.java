package com.weam.appointments.presentation.gui;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardFrame {

    public AdminDashboardFrame() {
        JFrame frame = new JFrame("Admin Dashboard");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(900, 600));

        UITheme.GradientPanel mainPanel = new UITheme.GradientPanel();
        mainPanel.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 230));
        card.setBorder(BorderFactory.createEmptyBorder(35, 45, 35, 45));

        JLabel title = UITheme.title("Admin Dashboard");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton viewSlotsBtn = UITheme.button("View Slots");
        JButton addSlotBtn = UITheme.button("Add Slot");
        JButton viewAppointmentsBtn = UITheme.button("View Appointments");
        JButton cancelAppointmentBtn = UITheme.button("Cancel Appointment");
        JButton logoutBtn = UITheme.button("Logout");

        Dimension buttonSize = new Dimension(260, 45);

        JButton[] buttons = {
                viewSlotsBtn,
                addSlotBtn,
                viewAppointmentsBtn,
                cancelAppointmentBtn,
                logoutBtn
        };

        for (JButton btn : buttons) {
            btn.setMaximumSize(buttonSize);
            btn.setPreferredSize(buttonSize);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        card.add(title);
        card.add(Box.createVerticalStrut(40));
        card.add(viewSlotsBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(addSlotBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(viewAppointmentsBtn);
        card.add(Box.createVerticalStrut(15));
        card.add(cancelAppointmentBtn);
        card.add(Box.createVerticalStrut(30));
        card.add(logoutBtn);

        mainPanel.add(card);

        viewSlotsBtn.addActionListener(e -> {
            frame.setVisible(false);
            new AvailableSlotsFrame(frame);
        });

        addSlotBtn.addActionListener(e -> {
            frame.setVisible(false);
            new AddSlotFrame(frame);
        });

        viewAppointmentsBtn.addActionListener(e -> {
            frame.setVisible(false);
            new AdminAppointmentsFrame(frame);
        });

        cancelAppointmentBtn.addActionListener(e -> {
            frame.setVisible(false);
            new AdminCancelAppointmentFrame(frame);
        });

        logoutBtn.addActionListener(e -> {
            frame.dispose();
            new LoginFrame();
        });

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }
}