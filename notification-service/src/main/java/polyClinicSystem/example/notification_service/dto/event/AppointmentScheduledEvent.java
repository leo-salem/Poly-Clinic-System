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
public class AppointmentScheduledEvent {
    private Long appointmentId;
    private String doctorKeycloakId;
    private String nurseKeycloakId;
    private String patientKeycloakId;
    private Long departmentId;
    private Long roomId;
    private LocalDate appointmentDate;
    private String period;
    private Instant timestamp;
}
