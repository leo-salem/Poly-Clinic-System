package polyClinicSystem.example.appointment_service.dto.event;
import lombok.*;

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
