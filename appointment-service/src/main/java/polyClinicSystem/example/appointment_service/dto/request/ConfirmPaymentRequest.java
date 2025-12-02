package polyClinicSystem.example.appointment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmPaymentRequest {
    /*
    the request come from frontend after front get the response of the stripe pay operation
    that is between the patient and stripe only stripe UI and front can't see the operation bt can see the result
    success or fail
    */


    @NotBlank(message = "Reservation token is required")
    private String reservationToken;

    @NotBlank(message = "Payment Intent ID is required")
    private String paymentIntentId;

    @NotNull(message = "payment ID is required")
    private Long paymentId;
    /*
     the id of the intent that the system will get a pay from this user which get it
     in the response which is reply on the reserve request to pay on it
     */
    /*
    and the front send it because he will receive the response if this transactional operation
    has succeeded and should tell the back that this intent that you gave me(IntentId,secretId) as a front
    and I showed StripeUi to him to pay has paid and take it back to verify
    */

}
