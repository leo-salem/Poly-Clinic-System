package polyClinicSystem.example.prescription_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePrescription {
    private Long recordId;
    private Long patientId;

    private String diagnose;
    private String medicine;
}
