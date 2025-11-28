package polyClinicSystem.example.appointment_service.dto.response;
import lombok.*;
import polyClinicSystem.example.appointment_service.model.enums.Period;
import polyClinicSystem.example.appointment_service.model.enums.Reason;
import polyClinicSystem.example.appointment_service.model.enums.Status;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private Long id;
    private String doctorKeycloakId;
    private String nurseKeycloakId;
    private String patientKeycloakId;
    private Long departmentId;
    private Long roomId;
    private Status status;
    private LocalDate appointmentDate;
    private Period period;
    private String reservationToken;
    private String paymentId;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Reason reason;
    private String notes;
}
