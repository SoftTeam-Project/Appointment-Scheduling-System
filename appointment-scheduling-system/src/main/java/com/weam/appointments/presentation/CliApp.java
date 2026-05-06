package com.weam.appointments.presentation;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentType;
import com.weam.appointments.notification.EmailNotificationObserver;
import com.weam.appointments.notification.NotificationService;
import com.weam.appointments.persistence.Db;
import com.weam.appointments.persistence.JdbcAppointmentRepository;
import com.weam.appointments.persistence.JdbcSlotRepository;
import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.service.AuthService;
import com.weam.appointments.service.BookingService;
import com.weam.appointments.service.ReminderService;
import com.weam.appointments.service.SlotService;

import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CliApp {

    private static final Logger LOGGER = Logger.getLogger(CliApp.class.getName());

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String EXIT_COMMAND = "exit";
    private static final String INVALID_CHOICE_MESSAGE = "Invalid choice.";

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
        SlotService slotService = createSlotService();
        BookingService bookingService = createBookingService();
        ReminderService reminderService = createReminderService();

        printHeader();

        String username = readUsername();
        if (isExitCommand(username)) {
            return;
        }

        String password = readPassword();

        var userRecordOpt = auth.login(username, password);
        if (userRecordOpt.isEmpty()) {
            out.println("Invalid credentials");
            return;
        }

        String role = userRecordOpt.get().role();
        out.println("Login successful! Role: " + role);

        runMenuLoop(username, role, slotService, bookingService, reminderService);
    }

    private SlotService createSlotService() {
        return new SlotService(new JdbcSlotRepository());
    }

    private BookingService createBookingService() {
        return new BookingService(new JdbcAppointmentRepository(), new JdbcSlotRepository());
    }

    private ReminderService createReminderService() {
        return new ReminderService(
                new JdbcAppointmentRepository(),
                new JdbcUserRepository(),
                notificationService,
                Clock.systemDefaultZone()
        );
    }

    private void printHeader() {
        out.println("=== Appointment Scheduling System ===");
    }

    private String readUsername() {
        out.print("Username (or exit): ");
        return sc.nextLine();
    }

    private String readPassword() {
        out.print("Password: ");
        return sc.nextLine();
    }

    private boolean isExitCommand(String username) {
        return username.equalsIgnoreCase(EXIT_COMMAND);
    }

    private void runMenuLoop(String username,
                             String role,
                             SlotService slotService,
                             BookingService bookingService,
                             ReminderService reminderService) {
        boolean keepRunning = true;

        while (keepRunning) {
            printMenu(role);
            String choice = sc.nextLine();

            keepRunning = handleMenuChoice(
                    choice,
                    username,
                    role,
                    slotService,
                    bookingService,
                    reminderService
            );
        }
    }

    private void printMenu(String role) {
        out.println("\n1) View available slots");
        out.println("2) Book appointment");
        out.println("3) Send reminders");
        out.println("4) Logout");
        out.println("5) Cancel appointment");

        if (isAdmin(role)) {
            out.println("6) Admin: Cancel any appointment");
        }

        out.print("Choice: ");
    }

    private boolean handleMenuChoice(String choice,
                                     String username,
                                     String role,
                                     SlotService slotService,
                                     BookingService bookingService,
                                     ReminderService reminderService) {
        switch (choice) {
            case "1":
                showAvailableSlots(slotService);
                return true;

            case "2":
                bookAppointment(username, bookingService);
                return true;

            case "3":
                sendReminders(reminderService);
                return true;

            case "4":
                logout(username);
                return false;

            case "5":
                cancelUserAppointment(username, bookingService);
                return true;

            case "6":
                cancelAppointmentAsAdmin(role, bookingService);
                return true;

            default:
                out.println(INVALID_CHOICE_MESSAGE);
                return true;
        }
    }

    private void showAvailableSlots(SlotService slotService) {
        var slots = slotService.viewAvailableSlots();

        if (slots.isEmpty()) {
            out.println("No available slots.");
            return;
        }

        out.println("Available slots:");
        for (var slot : slots) {
            out.println(slot.id + ") " + slot.date + " " + slot.time);
        }
    }

    private void bookAppointment(String username, BookingService bookingService) {
        int slotId = readInt("Enter slot id: ");
        int duration = readInt("Enter duration in minutes: ");
        int participants = readInt("Enter number of participants: ");

        AppointmentType type = readAppointmentType();
        if (type == null) {
            return;
        }

        boolean booked = bookingService.bookAppointment(
                slotId,
                username,
                duration,
                participants,
                type
        );

        printBookingResult(booked);
    }

    private int readInt(String prompt) {
        out.print(prompt);
        return Integer.parseInt(sc.nextLine());
    }

    private AppointmentType readAppointmentType() {
        printAppointmentTypeMenu();

        String typeChoice = sc.nextLine();

        switch (typeChoice) {
            case "1":
                return AppointmentType.URGENT;
            case "2":
                return AppointmentType.FOLLOW_UP;
            case "3":
                return AppointmentType.ASSESSMENT;
            case "4":
                return AppointmentType.VIRTUAL;
            case "5":
                return AppointmentType.IN_PERSON;
            case "6":
                return AppointmentType.INDIVIDUAL;
            case "7":
                return AppointmentType.GROUP;
            default:
                out.println("Invalid appointment type.");
                return null;
        }
    }

    private void printAppointmentTypeMenu() {
        out.println("Choose appointment type:");
        out.println("1) URGENT");
        out.println("2) FOLLOW_UP");
        out.println("3) ASSESSMENT");
        out.println("4) VIRTUAL");
        out.println("5) IN_PERSON");
        out.println("6) INDIVIDUAL");
        out.println("7) GROUP");
        out.print("Type choice: ");
    }

    private void printBookingResult(boolean booked) {
        if (booked) {
            out.println("Appointment booked successfully.");
            return;
        }

        out.println("Booking failed.");
        out.println("Check slot availability and type rules.");
        out.println("INDIVIDUAL requires exactly 1 participant.");
        out.println("GROUP requires at least 2 participants.");
        out.println("URGENT allows up to 30 minutes only.");
    }

    private void sendReminders(ReminderService reminderService) {
        reminderService.sendReminders();
        out.println("Reminders sent.");
    }

    private void logout(String username) {
        auth.logout(username);
        out.println("Logged out.");
    }

    private void cancelUserAppointment(String username, BookingService bookingService) {
        List<Appointment> userAppointments = bookingService.findFutureAppointmentsByUser(username);

        if (userAppointments.isEmpty()) {
            out.println("You have no upcoming appointments.");
            return;
        }

        out.println("Your upcoming appointments:");
        printUserAppointments(userAppointments);

        int appointmentId = readInt("Enter appointment ID to cancel: ");

        if (bookingService.cancelAppointment(appointmentId, username)) {
            out.println("Appointment cancelled successfully.");
        } else {
            out.println("Cancellation failed. Ensure the appointment is in the future and belongs to you.");
        }
    }

    private void printUserAppointments(List<Appointment> appointments) {
        for (Appointment appointment : appointments) {
            out.println(appointment.getId() + ") " +
                    appointment.getDate() + " " +
                    appointment.getTime() +
                    " (slot " + appointment.getSlotId() +
                    ", type " + appointment.getType() + ")");
        }
    }

    private void cancelAppointmentAsAdmin(String role, BookingService bookingService) {
        if (!isAdmin(role)) {
            out.println(INVALID_CHOICE_MESSAGE);
            return;
        }

        List<Appointment> allAppointments = bookingService.findAllFutureAppointments();

        if (allAppointments.isEmpty()) {
            out.println("No upcoming appointments.");
            return;
        }

        out.println("All upcoming appointments:");
        printAdminAppointments(allAppointments);

        int appointmentId = readInt("Enter appointment ID to cancel (as admin): ");

        if (bookingService.adminCancelAppointment(appointmentId)) {
            out.println("Appointment cancelled successfully by admin.");
        } else {
            out.println("Admin cancellation failed.");
        }
    }

    private void printAdminAppointments(List<Appointment> appointments) {
        for (Appointment appointment : appointments) {
            out.println(appointment.getId() + ") " +
                    appointment.getDate() + " " +
                    appointment.getTime() +
                    " (user: " + appointment.getUsername() +
                    ", slot " + appointment.getSlotId() +
                    ", type " + appointment.getType() + ")");
        }
    }

    private boolean isAdmin(String role) {
        return ROLE_ADMIN.equals(role);
    }

    @SuppressWarnings("java:S106")
    public static void main(String[] args) {
        new SchemaInitializer().init();

        try (Connection con = Db.getConnection();
             Statement st = con.createStatement()) {

            st.execute("DELETE FROM appointment_slots WHERE slot_date < date('now')");
            st.execute("INSERT INTO appointment_slots(slot_date, slot_time, capacity, booked_count) " +
                    "VALUES (date('now', '+1 day'), '14:00', 5, 0)");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize application data", e);
        }

        AuthService auth = new AuthService(new JdbcUserRepository());

        NotificationService notificationService = new NotificationService();
        notificationService.addObserver(new EmailNotificationObserver());

        new CliApp(auth, notificationService, System.in, System.out).runOnce();
    }
}