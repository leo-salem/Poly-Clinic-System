package polyClinicSystem.example.notification_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmedEvent {
    private String reservationToken;
    private Long appointmentId;
    private String patientKeycloakId;
    private String doctorKeycloakId;
    private String paymentIntentId;
    private LocalDate appointmentDate;
    private String period;
    private Instant timestamp;
}
