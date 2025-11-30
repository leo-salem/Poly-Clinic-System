package polyClinicSystem.example.appointment_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import polyClinicSystem.example.appointment_service.model.enums.Period;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayUnavailabilityRequest {

    @NotNull(message = "Doctor ID is required")
    private String doctorKeycloakId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotEmpty(message = "At least one period is required")
    private List<Period> periods;
}