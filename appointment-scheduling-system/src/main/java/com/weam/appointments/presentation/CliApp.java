package com.weam.appointments.presentation;

import com.weam.appointments.persistence.JdbcAppointmentRepository;
import com.weam.appointments.persistence.JdbcSlotRepository;
import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.service.AuthService;
import com.weam.appointments.service.BookingService;
import com.weam.appointments.service.SlotService;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class CliApp {

    private final AuthService auth;
    private final Scanner sc;
    private final PrintStream out;

    public CliApp(AuthService auth, InputStream in, PrintStream out) {
        this.auth = auth;
        this.sc = new Scanner(in);
        this.out = out;
    }

    public void runOnce() {
        SlotService slotService = new SlotService(new JdbcSlotRepository());
        BookingService bookingService =
                new BookingService(new JdbcAppointmentRepository(), new JdbcSlotRepository());

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
                out.println("3) Logout");
                out.print("Choice: ");
                String choice = sc.nextLine();

                if (choice.equals("1")) {
                    var slots = slotService.viewAvailableSlots();

                    if (slots.isEmpty()) {
                        out.println("No available slots.");
                    } else {
                        out.println("Available slots:");
                        for (var s : slots) {
                            out.println(s.id + ") " + s.date + " " + s.time);
                        }
                    }

                } else if (choice.equals("2")) {
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

                } else if (choice.equals("3")) {
                    auth.logout(u);
                    out.println("Logged out.");
                    return;

                } else {
                    out.println("Invalid choice.");
                }
            }

        } else {
            out.println("Invalid credentials");
        }
    }

    public static void main(String[] args) {
        new SchemaInitializer().init();
        AuthService auth = new AuthService(new JdbcUserRepository());
        new CliApp(auth, System.in, System.out).runOnce();
    }
}