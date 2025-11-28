package polyClinicSystem.example.appointment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import polyClinicSystem.example.appointment_service.dto.response.enums.Status;


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
    private String clientSecret;  // For Stripe Elements
    private BigDecimal amount;
    private String currency;
    private Status status;
    private Instant createdAt;
}