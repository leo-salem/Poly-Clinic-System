package polyClinicSystem.example.prescription_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "records")
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "medicalRecord",
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Prescription> prescriptions;

    public void AddPrescription(Prescription prescription) {
        if (this.prescriptions == null) {
            this.prescriptions = new HashSet<>();
        }
        prescriptions.add(prescription);
        prescription.setMedicalRecord(this);
    }

    public void RemovePrescription(Prescription prescription) {
        if (this.prescriptions != null) {
            prescriptions.remove(prescription);
            prescription.setMedicalRecord(null);
        }
    }
}
