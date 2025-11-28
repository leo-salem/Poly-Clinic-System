package polyClinicSystem.example.appointment_service.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminApprovalRequest {
    /*
    after the admin receive confirm request he will change the request to paid
    and start to search to room and nurse and after find these 2
    he will send this request by an accept or reject
      */

    @NotBlank(message = "Reservation token is required")
    private String reservationToken;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Nurse Keycloak ID is required")
    private String nurseKeycloakId;

    @NotNull(message = "Decision is required (APPROVE/REJECT)")
    private ApprovalDecision decision;

    private String rejectionReason;

    public enum ApprovalDecision {
        APPROVE,
        REJECT
    }
}
