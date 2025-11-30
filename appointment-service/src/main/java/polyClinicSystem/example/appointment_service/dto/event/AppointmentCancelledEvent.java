package polyClinicSystem.example.appointment_service.dto.event;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCancelledEvent {
    private Long appointmentId;
    private String reservationToken;
    private String doctorKeycloakId;
    private String patientKeycloakId;
    private String paymentIntentId;
    private LocalDate appointmentDate;
    private String period;
    private String cancellationReason;
    private Instant timestamp;
}