package polyClinicSystem.example.appointment_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polyClinicSystem.example.appointment_service.client.UserClient;
import polyClinicSystem.example.appointment_service.dto.request.AdminApprovalRequest;
import polyClinicSystem.example.appointment_service.dto.request.ConfirmPaymentRequest;
import polyClinicSystem.example.appointment_service.dto.request.ReserveSlotRequest;
import polyClinicSystem.example.appointment_service.dto.response.AppointmentResponse;
import polyClinicSystem.example.appointment_service.dto.response.AvailableSlotResponse;
import polyClinicSystem.example.appointment_service.dto.response.ReservationResponse;
import polyClinicSystem.example.appointment_service.service.Appointment.AppointmentService;
import polyClinicSystem.example.appointment_service.service.token.TokenService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final TokenService tokenService;
    private final UserClient userClient;
    @GetMapping("/available-slots")
    public ResponseEntity<AvailableSlotResponse> getAvailableSlots(
            @RequestParam String doctorKeycloakId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {

        String patientId = tokenService.extractUserId(request);
        AvailableSlotResponse response = appointmentService.getAvailableSlots(doctorKeycloakId, date, patientId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveSlot(
            @Valid @RequestBody ReserveSlotRequest request,
            HttpServletRequest httpRequest) {

        // Extract patient ID from token and set it
        String patientId = tokenService.extractUserId(httpRequest);
        request.setPatientKeycloakId(patientId);

        ReservationResponse response = appointmentService.reserveSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<Void> confirmPayment(@Valid @RequestBody ConfirmPaymentRequest request) {
        appointmentService.confirmPayment(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/admin/approve")
    public ResponseEntity<AppointmentResponse> adminApproval(@Valid @RequestBody AdminApprovalRequest request) {
        AppointmentResponse response = appointmentService.adminApproval(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable Long id) {
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(HttpServletRequest request) {
        String keycloakId = tokenService.extractUserId(request);
        String role = userClient.getUserByKeycloakId(keycloakId).getRole().toString();

        List<AppointmentResponse> appointments = appointmentService.getMyAppointments(keycloakId, role);
        return ResponseEntity.ok(appointments);
    }
}
