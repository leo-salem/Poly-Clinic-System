package polyClinicSystem.example.appointment_service.service.Appointment;

import polyClinicSystem.example.appointment_service.dto.request.AdminApprovalRequest;
import polyClinicSystem.example.appointment_service.dto.request.ConfirmPaymentRequest;
import polyClinicSystem.example.appointment_service.dto.request.ReserveSlotRequest;
import polyClinicSystem.example.appointment_service.dto.response.AppointmentResponse;
import polyClinicSystem.example.appointment_service.dto.response.AvailableSlotResponse;
import polyClinicSystem.example.appointment_service.dto.response.ReservationResponse;
import polyClinicSystem.example.appointment_service.model.entity.Appointment;
import polyClinicSystem.example.appointment_service.model.enums.Period;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    AppointmentResponse adminApproval(AdminApprovalRequest request);

    AppointmentResponse getAppointmentById(Long id);

    List<AppointmentResponse> getMyAppointments(String keycloakId, String role);

    AvailableSlotResponse getAvailableSlots(String doctorKeycloakId, LocalDate date, String requestingPatientId);

    ReservationResponse reserveSlot(ReserveSlotRequest request);

    void confirmPayment(ConfirmPaymentRequest request);





}
