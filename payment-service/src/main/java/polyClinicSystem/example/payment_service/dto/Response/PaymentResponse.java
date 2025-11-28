package polyClinicSystem.example.payment_service.dto.Response;
import lombok.*;
import polyClinicSystem.example.payment_service.model.enums.Status;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long appointmentId;
    private String patientKeycloakId;
    private String paymentIntentId;
    private String clientSecret;
    private BigDecimal amount;
    private String currency;
    private Status status;
    private Instant createdAt;
}