package polyClinicSystem.example.prescription_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrescriptionResponse {
    private Long id;
    private String diagnose;
    private String medicine;

    private Long doctorId;
    private Long patientId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long recordId;
}
