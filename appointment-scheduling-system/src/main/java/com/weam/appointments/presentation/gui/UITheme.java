package com.weam.appointments.presentation.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UITheme {

    public static final Color PRIMARY = new Color(52, 73, 94);
    public static final Color BUTTON = new Color(41, 128, 185);
    public static final Color BUTTON_HOVER = new Color(31, 97, 141);
    public static final Color BACKGROUND = new Color(245, 247, 250);
    public static final Color WHITE = Color.WHITE;

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public static void styleFrame(JFrame frame) {
        frame.getContentPane().setBackground(BACKGROUND);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(TITLE_FONT);
        label.setForeground(PRIMARY);
        return label;
    }
    public static void setupFrame(JFrame frame) {
        frame.setSize(1000, 700);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
    }

    public static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(BUTTON);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON);
            }
        });

        return button;
    }

    public static void styleLabel(JLabel label) {
        label.setFont(NORMAL_FONT);
        label.setForeground(PRIMARY);
    }

    public static void styleTextField(JTextField field) {
        field.setFont(NORMAL_FONT);
    }
    public static class GradientPanel extends JPanel {

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
    
    public static JPanel createCard() {

        JPanel panel = new JPanel();

        panel.setBackground(new Color(255, 255, 255, 230));

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        return panel;
    }
}