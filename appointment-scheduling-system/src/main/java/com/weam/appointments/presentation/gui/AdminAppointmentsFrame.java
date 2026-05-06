package com.weam.appointments.presentation.gui;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.persistence.JdbcAppointmentRepository;
import com.weam.appointments.persistence.JdbcSlotRepository;
import com.weam.appointments.service.BookingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminAppointmentsFrame {

    public AdminAppointmentsFrame(JFrame previousFrame) {

        JFrame frame = new JFrame("All Appointments");

        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(900, 600));

        UITheme.GradientPanel background = new UITheme.GradientPanel();
        background.setLayout(new BorderLayout(20, 20));
        background.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        frame.setContentPane(background);

        JButton backBtn = UITheme.button("Back");

        backBtn.addActionListener(e -> {

            frame.dispose();

            if (previousFrame != null) {
                previousFrame.setVisible(true);
            }
        });

        String[] columns = {
                "ID",
                "Slot ID",
                "Username",
                "Date",
                "Time",
                "Duration",
                "Participants",
                "Type",
                "Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);

        BookingService bookingService = new BookingService(
                new JdbcAppointmentRepository(),
                new JdbcSlotRepository()
        );

        List<Appointment> appointments =
                bookingService.findAllFutureAppointments();

        for (Appointment app : appointments) {

            model.addRow(new Object[]{
                    app.getId(),
                    app.getSlotId(),
                    app.getUsername(),
                    app.getDate(),
                    app.getTime(),
                    app.getDurationMinutes(),
                    app.getParticipants(),
                    app.getType(),
                    app.getStatus()
            });
        }

        JScrollPane scrollPane = new JScrollPane(table);

        background.add(backBtn, BorderLayout.NORTH);
        background.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}