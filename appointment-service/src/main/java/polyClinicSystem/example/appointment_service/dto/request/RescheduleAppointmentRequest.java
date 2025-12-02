package polyClinicSystem.example.appointment_service.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleAppointmentRequest {

    @NotNull(message = "Old appointment ID is required")
    private Long oldAppointmentId;

    @NotNull(message = "Reserve slot request is required")
    @Valid
    private ReserveSlotRequest reserveSlotRequest;

    @NotBlank(message = "Payment Intent ID is required")
    private String paymentIntentId;

    @NotNull(message = "payment ID is required")
    private Long paymentId;
}