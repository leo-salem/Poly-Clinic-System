package polyClinicSystem.example.appointment_service.dto.event;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCompletedEvent {
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