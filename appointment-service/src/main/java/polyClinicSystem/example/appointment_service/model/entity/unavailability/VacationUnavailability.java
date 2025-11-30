package polyClinicSystem.example.appointment_service.model.entity.unavailability;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vacation_unavailabilities")
public class VacationUnavailability extends Unavailability {

    @Column(name = "doctor_keycloak_id", nullable = false)
    private String doctorKeycloakId;

    @ElementCollection
    @CollectionTable(
            name = "vacation_day_of_week",
            joinColumns = @JoinColumn(name = "vacation_unavailability_id")
    )
    @Column(name = "day_of_week")
    private List<String> daysOfWeek = new ArrayList<>();
}
