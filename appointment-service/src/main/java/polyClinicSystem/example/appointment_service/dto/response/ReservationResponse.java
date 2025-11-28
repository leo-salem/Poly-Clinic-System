package polyClinicSystem.example.appointment_service.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private String reservationToken;
    private Instant expiresAt;
    private Long appointmentId;
    private String message;
}
