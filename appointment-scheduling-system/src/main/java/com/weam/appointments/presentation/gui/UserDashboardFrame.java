package com.weam.appointments.presentation.gui;

import javax.swing.*;
import java.awt.*;

public class UserDashboardFrame {

    private final String username;

    public UserDashboardFrame(String username) {
        this.username = username;

        JFrame frame = new JFrame("Student Dashboard");

        frame.setSize(1000, 700);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        UITheme.GradientPanel background = new UITheme.GradientPanel();
        background.setLayout(new GridBagLayout());
        frame.setContentPane(background);

        JPanel card = new JPanel();
        card.setLayout(null);
        card.setPreferredSize(new Dimension(430, 430));
        card.setBackground(new Color(255, 255, 255, 230));

        JLabel title = new JLabel("Welcome " + username, SwingConstants.CENTER);
        title.setBounds(65, 30, 300, 35);

        JButton availableSlotsBtn = new JButton("Available Slots");
        availableSlotsBtn.setBounds(105, 90, 220, 40);

        JButton bookAppointmentBtn = new JButton("Book Appointment");
        bookAppointmentBtn.setBounds(105, 140, 220, 40);

        JButton myAppointmentsBtn = new JButton("My Appointments");
        myAppointmentsBtn.setBounds(105, 190, 220, 40);

        JButton cancelAppointmentBtn = new JButton("Cancel Appointment");
        cancelAppointmentBtn.setBounds(105, 240, 220, 40);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(105, 310, 220, 40);

        availableSlotsBtn.addActionListener(e -> {
            frame.setVisible(false);
            new AvailableSlotsFrame(frame);
        });

        bookAppointmentBtn.addActionListener(e -> {
            frame.setVisible(false);
        

            new BookAppointmentFrame(username, frame);
        });

        myAppointmentsBtn.addActionListener(e -> {
        	frame.setVisible(false);
        	new MyAppointmentsFrame(username, frame);
        });

        cancelAppointmentBtn.addActionListener(e -> {
            frame.setVisible(false);
           

            new CancelAppointmentFrame(username, frame);
        });

        logoutBtn.addActionListener(e -> {
            frame.dispose();
            new LoginFrame();
        });

        card.add(title);
        card.add(availableSlotsBtn);
        card.add(bookAppointmentBtn);
        card.add(myAppointmentsBtn);
        card.add(cancelAppointmentBtn);
        card.add(logoutBtn);

        background.add(card);

        frame.setVisible(true);
    }
}