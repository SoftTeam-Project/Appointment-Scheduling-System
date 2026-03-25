package com.weam.appointments.presentation;

import java.util.List;
import com.weam.appointments.domain.Appointment;
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
                    
                 case "5":
                    List<Appointment> userApps = bookingService.findFutureAppointmentsByUser(u);
                    if (userApps.isEmpty()) {
                    	
                        out.println("You have no upcoming appointments.");
                        break;
                    }
                    out.println("Your upcoming appointments:");
                    for (Appointment a : userApps) {
                    	
                        out.println(a.getId() + ") " + a.getDate() + " " + a.getTime() + " (slot " + a.getSlotId() + ")");
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
                     // عرض جميع المواعيد المستقبلية
                     List<Appointment> allApps = bookingService.findAllFutureAppointments();
                     if (allApps.isEmpty()) {
                         out.println("No upcoming appointments.");
                         break;
                     }
                     out.println("All upcoming appointments:");
                     for (Appointment a : allApps) {
                         out.println(a.getId() + ") " + a.getDate() + " " + a.getTime() +
                                 " (user: " + a.getUsername() + ", slot " + a.getSlotId() + ")");
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