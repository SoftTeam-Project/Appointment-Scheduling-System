package com.weam.appointments.presentation.gui;

import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame {

    public RegisterFrame(JFrame previousFrame) {
        JFrame frame = new JFrame("Create Account");

        frame.setSize(1000, 700);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);

        UITheme.GradientPanel background = new UITheme.GradientPanel();
        background.setLayout(new GridBagLayout());
        frame.setContentPane(background);

        JPanel card = new JPanel();
        card.setLayout(null);
        card.setPreferredSize(new Dimension(460, 420));
        card.setBackground(new Color(255, 255, 255, 230));

        JLabel title = new JLabel("Create Student Account", SwingConstants.CENTER);
        title.setBounds(50, 30, 360, 35);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(60, 90, 120, 25);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(190, 90, 200, 30);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(60, 140, 120, 25);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(190, 140, 200, 30);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(60, 190, 120, 25);

        JTextField emailField = new JTextField();
        emailField.setBounds(190, 190, 200, 30);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(90, 280, 130, 40);

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(240, 280, 130, 40);

        AuthService authService = new AuthService(new JdbcUserRepository());

        registerBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String email = emailField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill all fields");
                return;
            }

            boolean created = authService.register(username, password, "STUDENT", email);

            if (created) {
                JOptionPane.showMessageDialog(frame, "Account created successfully");
                frame.dispose();

                if (previousFrame != null) {
                    previousFrame.setVisible(true);
                }

            } else {
                JOptionPane.showMessageDialog(frame, "Username already exists or registration failed");
            }
        });

        backBtn.addActionListener(e -> {
            frame.dispose();

            if (previousFrame != null) {
                previousFrame.setVisible(true);
            }
        });

        card.add(title);
        card.add(usernameLabel);
        card.add(usernameField);
        card.add(passwordLabel);
        card.add(passwordField);
        card.add(emailLabel);
        card.add(emailField);
        card.add(registerBtn);
        card.add(backBtn);

        background.add(card);

        frame.setVisible(true);
    }
}