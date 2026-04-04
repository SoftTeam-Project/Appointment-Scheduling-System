package com.weam.appointments.presentation;

import java.util.List;
import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentType;
import com.weam.appointments.persistence.Db;
import com.weam.appointments.persistence.JdbcAppointmentRepository;
import com.weam.appointments.persistence.JdbcSlotRepository;
import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.service.AuthService;
import com.weam.appointments.service.*;
import com.weam.appointments.notification.NotificationService;
import com.weam.appointments.notification.EmailNotificationObserver;
import java.time.Clock;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CliApp {

    private final AuthService auth;
    private final NotificationService notificationService;
    private final Scanner sc;
    private final PrintStream out;

    public CliApp(AuthService auth, NotificationService notificationService, InputStream in, PrintStream out) {
        this.auth = auth;
        this.notificationService = notificationService;
        this.sc = new Scanner(in);
        this.out = out;
    }

    public void runOnce() {
        SlotService slotService = new SlotService(new JdbcSlotRepository());
        BookingService bookingService =
                new BookingService(new JdbcAppointmentRepository(), new JdbcSlotRepository());

        ReminderService reminderService = new ReminderService(
                new JdbcAppointmentRepository(),
                new JdbcUserRepository(),
                notificationService,
                Clock.systemDefaultZone()
        );

        out.println("=== Appointment Scheduling System ===");
        out.print("Username (or exit): ");
        String u = sc.nextLine();

        if (u.equalsIgnoreCase("exit")) {
            return;
        }

        out.print("Password: ");
        String p = sc.nextLine();

        var userRecordOpt = auth.login(u, p);
        if (userRecordOpt.isPresent()) {
            var userRecord = userRecordOpt.get();
            String role = userRecord.role();
            out.println("Login successful! Role: " + role);

            while (true) {
                out.println("\n1) View available slots");
                out.println("2) Book appointment");
                out.println("3) Send reminders");
                out.println("4) Logout");
                out.println("5) Cancel appointment");
                if ("ADMIN".equals(role)) {
                    out.println("6) Admin: Cancel any appointment");
                }
                out.print("Choice: ");
                String choice = sc.nextLine();

                switch (choice) {
                    case "1":
                        var slots = slotService.viewAvailableSlots();
                        if (slots.isEmpty()) {
                            out.println("No available slots.");
                        } else {
                            out.println("Available slots:");
                            for (var s : slots) {
                                out.println(s.id + ") " + s.date + " " + s.time);
                            }
                        }
                        break;

                    case "2":
                        out.print("Enter slot id: ");
                        int slotId = Integer.parseInt(sc.nextLine());

                        out.print("Enter duration in minutes: ");
                        int duration = Integer.parseInt(sc.nextLine());

                        out.print("Enter number of participants: ");
                        int participants = Integer.parseInt(sc.nextLine());

                        out.println("Choose appointment type:");
                        out.println("1) URGENT");
                        out.println("2) FOLLOW_UP");
                        out.println("3) ASSESSMENT");
                        out.println("4) VIRTUAL");
                        out.println("5) IN_PERSON");
                        out.println("6) INDIVIDUAL");
                        out.println("7) GROUP");
                        out.print("Type choice: ");
                        String typeChoice = sc.nextLine();

                        AppointmentType type = null;

                        switch (typeChoice) {
                            case "1":
                                type = AppointmentType.URGENT;
                                break;
                            case "2":
                                type = AppointmentType.FOLLOW_UP;
                                break;
                            case "3":
                                type = AppointmentType.ASSESSMENT;
                                break;
                            case "4":
                                type = AppointmentType.VIRTUAL;
                                break;
                            case "5":
                                type = AppointmentType.IN_PERSON;
                                break;
                            case "6":
                                type = AppointmentType.INDIVIDUAL;
                                break;
                            case "7":
                                type = AppointmentType.GROUP;
                                break;
                            default:
                                out.println("Invalid appointment type.");
                                break;
                        }

                        if (type == null) {
                            break;
                        }

                        boolean booked = bookingService.bookAppointment(slotId, u, duration, participants, type);

                        if (booked) {
                            out.println("Appointment booked successfully.");
                        } else {
                            out.println("Booking failed.");
                            out.println("Check slot availability and type rules.");
                            out.println("INDIVIDUAL requires exactly 1 participant.");
                            out.println("GROUP requires at least 2 participants.");
                            out.println("URGENT allows up to 30 minutes only.");
                        }
                        break;

                    case "3":
                        reminderService.sendReminders();
                        out.println("Reminders sent.");
                        break;

                    case "4":
                        auth.logout(u);
                        out.println("Logged out.");
                        return;

                    case "5":
                        List<Appointment> userApps = bookingService.findFutureAppointmentsByUser(u);
                        if (userApps.isEmpty()) {
                            out.println("You have no upcoming appointments.");
                            break;
                        }
                        out.println("Your upcoming appointments:");
                        for (Appointment a : userApps) {
                            out.println(a.getId() + ") " + a.getDate() + " " + a.getTime() +
                                    " (slot " + a.getSlotId() + ", type " + a.getType() + ")");
                        }
                        out.print("Enter appointment ID to cancel: ");
                        int appId = Integer.parseInt(sc.nextLine());
                        if (bookingService.cancelAppointment(appId, u)) {
                            out.println("Appointment cancelled successfully.");
                        } else {
                            out.println("Cancellation failed. Ensure the appointment is in the future and belongs to you.");
                        }
                        break;

                    case "6":
                        if (!"ADMIN".equals(role)) {
                            out.println("Invalid choice.");
                            break;
                        }

                        List<Appointment> allApps = bookingService.findAllFutureAppointments();
                        if (allApps.isEmpty()) {
                            out.println("No upcoming appointments.");
                            break;
                        }

                        out.println("All upcoming appointments:");
                        for (Appointment a : allApps) {
                            out.println(a.getId() + ") " + a.getDate() + " " + a.getTime() +
                                    " (user: " + a.getUsername() + ", slot " + a.getSlotId() +
                                    ", type " + a.getType() + ")");
                        }

                        out.print("Enter appointment ID to cancel (as admin): ");
                        int adminAppId = Integer.parseInt(sc.nextLine());
                        if (bookingService.adminCancelAppointment(adminAppId)) {
                            out.println("Appointment cancelled successfully by admin.");
                        } else {
                            out.println("Admin cancellation failed.");
                        }
                        break;

                    default:
                        out.println("Invalid choice.");
                }
            }
        } else {
            out.println("Invalid credentials");
        }
    }

    public static void main(String[] args) {
        new SchemaInitializer().init();

        try (Connection con = Db.getConnection();
             Statement st = con.createStatement()) {
            st.execute("DELETE FROM appointment_slots WHERE slot_date < date('now')");
            st.execute("INSERT INTO appointment_slots(slot_date, slot_time, capacity, booked_count) " +
                    "VALUES (date('now', '+1 day'), '14:00', 5, 0)");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        AuthService auth = new AuthService(new JdbcUserRepository());

        NotificationService notificationService = new NotificationService();
        notificationService.addObserver(new EmailNotificationObserver());

        new CliApp(auth, notificationService, System.in, System.out).runOnce();
    }
   
    
}