package polyClinicSystem.example.appointment_service.service.Appointment;

import polyClinicSystem.example.appointment_service.dto.request.*;
import polyClinicSystem.example.appointment_service.dto.response.AppointmentResponse;
import polyClinicSystem.example.appointment_service.dto.response.AvailableSlotResponse;
import polyClinicSystem.example.appointment_service.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    AvailableSlotResponse getAvailableSlots(String doctorKeycloakId, LocalDate date, String requestingPatientId);

    ReservationResponse reserveSlot(ReserveSlotRequest request);

    ReservationResponse rescheduleAppointment(RescheduleAppointmentRequest request);

    void confirmPayment(ConfirmPaymentRequest request);

    AppointmentResponse adminApproval(AdminApprovalRequest request);

    void createDayUnavailability(DayUnavailabilityRequest request);

    void createVacationUnavailability(VacationUnavailabilityRequest request);

    AppointmentResponse getAppointmentById(Long id);

    List<AppointmentResponse> getMyAppointments(String keycloakId, String role);
}