package polyClinicSystem.example.appointment_service.service.Appointment;

import jakarta.servlet.http.HttpServletRequest;
import polyClinicSystem.example.appointment_service.dto.request.*;
import polyClinicSystem.example.appointment_service.dto.response.AppointmentResponse;
import polyClinicSystem.example.appointment_service.dto.response.AvailableSlotResponse;
import polyClinicSystem.example.appointment_service.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;
public interface AppointmentService {

    AvailableSlotResponse getAvailableSlots(String doctorKeycloakId, LocalDate date, String requestingPatientId, HttpServletRequest request);
    ReservationResponse reserveSlot(ReserveSlotRequest request, HttpServletRequest httpRequest);
    ReservationResponse rescheduleAppointment(RescheduleAppointmentRequest request, HttpServletRequest httpRequest);
    void createDayUnavailability(DayUnavailabilityRequest request, HttpServletRequest httpRequest);
    void createVacationUnavailability(VacationUnavailabilityRequest request, HttpServletRequest httpRequest);
    void confirmPayment(ConfirmPaymentRequest request, HttpServletRequest httpRequest);
    AppointmentResponse adminApproval(AdminApprovalRequest request, HttpServletRequest httpRequest);
    AppointmentResponse getAppointmentById(Long id, HttpServletRequest httpRequest);
    List<AppointmentResponse> getMyAppointments(String keycloakId, String role, HttpServletRequest httpRequest);
}