package polyClinicSystem.example.prescription_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalRecordResponse {
    private Long id;
    private String patientId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PrescriptionSlimDTO> prescriptions;
}
