package com.weam.appointments.presentation;

import com.weam.appointments.persistence.Db;
import com.weam.appointments.persistence.JdbcAppointmentRepository;
import com.weam.appointments.persistence.JdbcSlotRepository;
import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.service.AuthService;
import com.weam.appointments.service.*;
import com.weam.appointments.notification.NotificationService;
import com.weam.appointments.notification.EmailNotificationObserver;
import com.weam.appointments.service.BookingService;
import com.weam.appointments.service.SlotService;
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

    public CliApp(AuthService auth,NotificationService notificationService, InputStream in, PrintStream out) {
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

        if (auth.login(u, p)) {
            out.println("Login successful!");

            while (true) {
                out.println("\n1) View available slots");
                out.println("2) Book appointment");
                out.println("3) Send reminders");
                out.println("4) Logout");
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

                    boolean booked = bookingService.bookAppointment(slotId, u, duration, participants);
                    
                    if (booked) {
                        out.println("Appointment booked successfully.");
                    } else {
                        out.println("Booking failed.");
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
   // System.setProperty("mail.smtp.ssl.trust", "*");
    try (Connection con = Db.getConnection();
    		Statement st = con.createStatement()) {
    	st.execute("DELETE FROM appointment_slots WHERE slot_date < date('now')");
    	st.execute("INSERT INTO appointment_slots(slot_date, slot_time, capacity, booked_count) " +
                "VALUES (date('now', '+1 day'), '14:00', 1, 0)");
    	
    }catch (SQLException e) {
        e.printStackTrace();
    }
    
    AuthService auth = new AuthService(new JdbcUserRepository());

    NotificationService notificationService = new NotificationService();
    
    notificationService.addObserver(new EmailNotificationObserver());
    new CliApp(auth, notificationService, System.in, System.out).runOnce();
}
}