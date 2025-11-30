package polyClinicSystem.example.notification_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRejectedEvent {
    private Long appointmentId;
    private String reservationToken;
    private String patientKeycloakId;
    private String paymentIntentId;
    private String rejectionReason;
    private Instant timestamp;
}
