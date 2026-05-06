package com.weam.appointments.service;

import com.weam.appointments.domain.Appointment;
import com.weam.appointments.domain.AppointmentType;
import com.weam.appointments.persistence.AppointmentRepository;
import com.weam.appointments.persistence.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceBranchTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private SlotRepository slotRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(appointmentRepository, slotRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1, INDIVIDUAL",
            "30, 0, INDIVIDUAL",
            "30, 2, INDIVIDUAL",
            "30, 1, GROUP",
            "45, 1, URGENT"
    })
    void bookAppointment_shouldReturnFalseForInvalidInputs(
            int duration,
            int participants,
            AppointmentType type
    ) {
        boolean result = bookingService.bookAppointment(
                1,
                "student",
                duration,
                participants,
                type
        );

        assertFalse(result);
        verifyNoInteractions(slotRepository);
    }

    @Test
    void bookAppointment_shouldReturnFalseWhenSlotDoesNotExist() {
        when(slotRepository.findById(1)).thenReturn(Optional.empty());

        boolean result = bookingService.bookAppointment(
                1,
                "student",
                30,
                1,
                AppointmentType.INDIVIDUAL
        );

        assertFalse(result);
        verify(slotRepository).findById(1);
    }

    @Test
    void cancelAppointment_shouldReturnFalseWhenAppointmentDoesNotExist() {
        when(appointmentRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = bookingService.cancelAppointment(99, "student");

        assertFalse(result);
        verify(appointmentRepository).findById(99);
    }

    @Test
    void cancelAppointment_shouldReturnFalseWhenAppointmentBelongsToAnotherUser() {
        Appointment appointment = Appointment.builder()
                .id(1)
                .slotId(1)
                .username("otherStudent")
                .date("2099-12-20")
                .time("10:00")
                .durationMinutes(30)
                .participants(1)
                .status("Confirmed")
                .type(AppointmentType.INDIVIDUAL)
                .build();

        when(appointmentRepository.findById(1)).thenReturn(Optional.of(appointment));

        boolean result = bookingService.cancelAppointment(1, "student");

        assertFalse(result);
        verify(appointmentRepository).findById(1);
    }

    @Test
    void cancelAppointment_shouldReturnFalseWhenAppointmentIsInThePast() {
        Appointment appointment = Appointment.builder()
                .id(1)
                .slotId(1)
                .username("student")
                .date("2000-01-01")
                .time("10:00")
                .durationMinutes(30)
                .participants(1)
                .status("Confirmed")
                .type(AppointmentType.INDIVIDUAL)
                .build();

        when(appointmentRepository.findById(1)).thenReturn(Optional.of(appointment));

        boolean result = bookingService.cancelAppointment(1, "student");

        assertFalse(result);
        verify(appointmentRepository).findById(1);
    }

    @Test
    void adminCancelAppointment_shouldReturnFalseWhenAppointmentDoesNotExist() {
        when(appointmentRepository.findById(77)).thenReturn(Optional.empty());

        boolean result = bookingService.adminCancelAppointment(77);

        assertFalse(result);
        verify(appointmentRepository).findById(77);
    }
}