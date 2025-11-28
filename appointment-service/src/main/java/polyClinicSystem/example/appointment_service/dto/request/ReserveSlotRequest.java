package polyClinicSystem.example.appointment_service.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import polyClinicSystem.example.appointment_service.model.enums.Period;
import polyClinicSystem.example.appointment_service.model.enums.Reason;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveSlotRequest {
    /*
     the patient can use this request to reserve a slot by acquired lock by redis (SetIfAbsent)
     for a while (15 minutes )
     and the appointment whose created will have pending status till paid and will be paid status
     if not paid the status will be expired
     */

    @NotNull(message = "Doctor ID is required")
    private String doctorKeycloakId;

    @NotNull(message = "Appointment date is required")
    private LocalDate appointmentDate;

    @NotNull(message = "Period is required")
    private Period period;

    @NotNull(message = "Reason is required")
    private Reason reason; //(examination ot Reexamination )

    @NotNull(message = "notes is required")
    private String notes;

    @NotNull(message = "Patient ID is required")
    private String patientKeycloakId;

}
