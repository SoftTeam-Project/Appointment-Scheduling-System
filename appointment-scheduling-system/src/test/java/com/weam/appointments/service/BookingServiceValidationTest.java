package com.weam.appointments.service;

import com.weam.appointments.domain.AppointmentSlot;
import com.weam.appointments.domain.AppointmentType;
import com.weam.appointments.persistence.AppointmentRepository;
import com.weam.appointments.persistence.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceValidationTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private SlotRepository slotRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(appointmentRepository, slotRepository);
    }

    @Test
    void isValidDuration_shouldReturnTrueForValidDuration() {
        assertTrue(bookingService.isValidDuration(30));
        assertTrue(bookingService.isValidDuration(120));
    }

    @Test
    void isValidDuration_shouldReturnFalseForInvalidDuration() {
        assertFalse(bookingService.isValidDuration(0));
        assertFalse(bookingService.isValidDuration(121));
    }

    @Test
    void isValidParticipants_shouldReturnTrueForValidParticipants() {
        assertTrue(bookingService.isValidParticipants(1));
        assertTrue(bookingService.isValidParticipants(5));
    }

    @Test
    void isValidParticipants_shouldReturnFalseForInvalidParticipants() {
        assertFalse(bookingService.isValidParticipants(0));
        assertFalse(bookingService.isValidParticipants(6));
    }

    @Test
    void isSlotAvailable_shouldReturnTrueWhenSlotExistsInAvailableSlots() {
        AppointmentSlot slot = new AppointmentSlot(1, "2099-01-01", "10:00", 5, 0);

        when(slotRepository.findAvailableSlots()).thenReturn(List.of(slot));

        assertTrue(bookingService.isSlotAvailable(1));
    }

    @Test
    void isSlotAvailable_shouldReturnFalseWhenSlotDoesNotExistInAvailableSlots() {
        AppointmentSlot slot = new AppointmentSlot(1, "2099-01-01", "10:00", 5, 0);

        when(slotRepository.findAvailableSlots()).thenReturn(List.of(slot));

        assertFalse(bookingService.isSlotAvailable(99));
    }

    @Test
    void isValidForType_shouldReturnTrueForValidIndividualAppointment() {
        assertTrue(bookingService.isValidForType(AppointmentType.INDIVIDUAL, 30, 1));
    }

    @Test
    void isValidForType_shouldReturnFalseForInvalidIndividualAppointment() {
        assertFalse(bookingService.isValidForType(AppointmentType.INDIVIDUAL, 30, 2));
    }

    @Test
    void isValidForType_shouldReturnTrueForValidGroupAppointment() {
        assertTrue(bookingService.isValidForType(AppointmentType.GROUP, 60, 2));
    }

    @Test
    void isValidForType_shouldReturnFalseForInvalidGroupAppointment() {
        assertFalse(bookingService.isValidForType(AppointmentType.GROUP, 60, 1));
    }

    @Test
    void isValidForType_shouldReturnTrueForValidUrgentAppointment() {
        assertTrue(bookingService.isValidForType(AppointmentType.URGENT, 30, 1));
    }

    @Test
    void isValidForType_shouldReturnFalseForInvalidUrgentAppointment() {
        assertFalse(bookingService.isValidForType(AppointmentType.URGENT, 31, 1));
    }

    @Test
    void isValidForType_shouldReturnTrueForOtherAppointmentTypes() {
        assertTrue(bookingService.isValidForType(AppointmentType.FOLLOW_UP, 60, 1));
        assertTrue(bookingService.isValidForType(AppointmentType.ASSESSMENT, 60, 1));
        assertTrue(bookingService.isValidForType(AppointmentType.VIRTUAL, 60, 1));
        assertTrue(bookingService.isValidForType(AppointmentType.IN_PERSON, 60, 1));
    }
}