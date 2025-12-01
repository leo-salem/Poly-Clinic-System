package polyClinicSystem.example.appointment_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polyClinicSystem.example.appointment_service.dto.request.*;
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

    @GetMapping("/available-slots")
    public ResponseEntity<AvailableSlotResponse> getAvailableSlots(
            @RequestParam String doctorKeycloakId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {

        String patientId = tokenService.extractUserId(request);
        AvailableSlotResponse response = appointmentService.getAvailableSlots(doctorKeycloakId, date, patientId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveSlot(
            @Valid @RequestBody ReserveSlotRequest request,
            HttpServletRequest httpRequest) {

        String patientId = tokenService.extractUserId(httpRequest);
        request.setPatientKeycloakId(patientId);

        ReservationResponse response = appointmentService.reserveSlot(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/reschedule")
    public ResponseEntity<ReservationResponse> rescheduleAppointment(
            @Valid @RequestBody RescheduleAppointmentRequest request,
            HttpServletRequest httpRequest) {

        ReservationResponse response = appointmentService.rescheduleAppointment(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<Void> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest request,
            HttpServletRequest httpRequest) {

        appointmentService.confirmPayment(request, httpRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/admin/approve")
    public ResponseEntity<AppointmentResponse> adminApproval(
            @Valid @RequestBody AdminApprovalRequest request,
            HttpServletRequest httpRequest) {

        AppointmentResponse response = appointmentService.adminApproval(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/day-unavailability")
    public ResponseEntity<Void> createDayUnavailability(
            @Valid @RequestBody DayUnavailabilityRequest request,
            HttpServletRequest httpRequest) {

        appointmentService.createDayUnavailability(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admin/vacation-unavailability")
    public ResponseEntity<Void> createVacationUnavailability(
            @Valid @RequestBody VacationUnavailabilityRequest request,
            HttpServletRequest httpRequest) {

        appointmentService.createVacationUnavailability(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        AppointmentResponse response = appointmentService.getAppointmentById(id, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(HttpServletRequest request) {
        String keycloakId = tokenService.extractUserId(request);
        String role = tokenService.extractUserRole(request);

        List<AppointmentResponse> appointments = appointmentService.getMyAppointments(keycloakId, role, request);
        return ResponseEntity.ok(appointments);
    }
}