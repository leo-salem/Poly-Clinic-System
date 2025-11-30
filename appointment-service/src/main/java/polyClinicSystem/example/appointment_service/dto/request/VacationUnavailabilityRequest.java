package polyClinicSystem.example.appointment_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacationUnavailabilityRequest {

    @NotNull(message = "Doctor ID is required")
    private String doctorKeycloakId;

    @NotEmpty(message = "At least one day of week is required")
    private List<String> daysOfWeek; // e.g., ["Monday", "Tuesday"]
}