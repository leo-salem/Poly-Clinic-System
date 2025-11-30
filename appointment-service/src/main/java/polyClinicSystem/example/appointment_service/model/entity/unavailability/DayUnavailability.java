package polyClinicSystem.example.appointment_service.model.entity.unavailability;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import polyClinicSystem.example.appointment_service.model.enums.Period;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "specific_unavailabilities")
public class DayUnavailability extends Unavailability {

    @Column(name = "doctor_keycloak_id", nullable = false)
    private String doctorKeycloakId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @ElementCollection
    @CollectionTable(
            name = "day_unavailability_periods",
            joinColumns = @JoinColumn(name = "day_unavailability_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "period")
    private List<Period> periods = new ArrayList<>();
}
