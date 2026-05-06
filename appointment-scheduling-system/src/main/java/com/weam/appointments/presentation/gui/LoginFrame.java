package com.weam.appointments.presentation.gui;

import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.persistence.UserRecord;
import com.weam.appointments.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class LoginFrame {

    private final AuthService authService;

    public LoginFrame() {
        new SchemaInitializer().init();
        this.authService = new AuthService(new JdbcUserRepository());
        createUI();
    }

    private void createUI() {
        JFrame frame = new JFrame("Appointment System - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(900, 600));

        JPanel background = new GradientPanel();
        background.setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(600, 660));
        card.setBackground(new Color(255, 255, 255, 230));
        card.setLayout(null);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235), 1));

        JLabel title = new JLabel("Appointment Scheduling System", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 30));
        title.setForeground(new Color(220, 53, 69));
        title.setBounds(20, 30, 560, 50);

        JLabel subtitle = new JLabel("Login to your appointment account", SwingConstants.CENTER);
        subtitle.setFont(new Font("Serif", Font.PLAIN, 16));
        subtitle.setForeground(new Color(60, 70, 90));
        subtitle.setBounds(100, 78, 400, 25);

        JLabel userLabel = label("Username");
        userLabel.setBounds(90, 125, 120, 25);

        JTextField userField = textField();
        userField.setBounds(90, 155, 420, 42);

        JLabel passLabel = label("Password");
        passLabel.setBounds(90, 210, 120, 25);

        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225)),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        passField.setBounds(90, 240, 420, 42);

        JLabel roleTitle = label("Select Role");
        roleTitle.setBounds(90, 300, 150, 25);

        final String[] selectedRole = {"STUDENT"};

        JPanel studentCard = roleCard("👨‍🎓", "STUDENT", true);
        studentCard.setBounds(90, 335, 180, 95);

        JPanel adminCard = roleCard("👨‍💼", "ADMIN", false);
        adminCard.setBounds(330, 335, 180, 95);

        studentCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectedRole[0] = "STUDENT";
                selectRoleCard(studentCard, true);
                selectRoleCard(adminCard, false);
            }
        });

        adminCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectedRole[0] = "ADMIN";
                selectRoleCard(adminCard, true);
                selectRoleCard(studentCard, false);
            }
        });

        JButton loginBtn = button("Login");
        loginBtn.setBounds(170, 470, 260, 45);

        JButton registerBtn = button("Create Account");
        registerBtn.setBounds(170, 525, 260, 45);

        registerBtn.addActionListener(e -> {
            frame.setVisible(false);
            new RegisterFrame(frame);
        });

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            String role = selectedRole[0];

            Optional<UserRecord> user = authService.login(username, password);

            if (user.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Wrong username or password");
                return;
            }

            String actualRole = user.get().role();

            if (!actualRole.equalsIgnoreCase(role)) {
                JOptionPane.showMessageDialog(frame, "This account is not " + role);
                return;
            }

            frame.dispose();

            if (actualRole.equalsIgnoreCase("ADMIN")) {
                new AdminDashboardFrame();
            } else {
                new UserDashboardFrame(username);
            }
        });

        card.add(title);
        card.add(subtitle);
        card.add(userLabel);
        card.add(userField);
        card.add(passLabel);
        card.add(passField);
        card.add(roleTitle);
        card.add(studentCard);
        card.add(adminCard);
        card.add(loginBtn);
        card.add(registerBtn);

        background.add(card);
        frame.add(background);
        frame.setVisible(true);
    }

    private JPanel roleCard(String icon, String text, boolean selected) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        iconLabel.setBounds(0, 12, 180, 35);

        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        textLabel.setBounds(0, 55, 180, 25);

        panel.add(iconLabel);
        panel.add(textLabel);

        selectRoleCard(panel, selected);
        return panel;
    }

    private void selectRoleCard(JPanel panel, boolean selected) {
        JLabel textLabel = (JLabel) panel.getComponent(1);

        if (selected) {
            panel.setBackground(new Color(108, 92, 231));
            panel.setBorder(BorderFactory.createLineBorder(new Color(108, 92, 231), 3));
            textLabel.setForeground(Color.WHITE);
        } else {
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 2));
            textLabel.setForeground(new Color(40, 40, 40));
        }
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(new Color(25, 42, 86));
        return label;
    }

    private JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225)),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        return field;
    }

    private JButton button(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(108, 92, 231));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth();
            int h = getHeight();

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(116, 185, 255),
                    w, h, new Color(255, 177, 193)
            );

            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(255, 255, 255, 70));
            g2.fillOval(-150, -100, 450, 450);
            g2.fillOval(w - 300, h - 250, 500, 500);

            g2.setColor(new Color(108, 92, 231, 90));
            g2.fillOval(80, h - 220, 300, 300);
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}