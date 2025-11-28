package polyClinicSystem.example.payment_service.dto.Request;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters (e.g., USD)")
    private String currency;

    @NotBlank(message = "Patient ID is required")
    private String patientKeycloakId;

    private String customerId; // Optional Stripe customer ID

    private Long appointmentId;

    private String paymentMethodId;

    private String description;
}

