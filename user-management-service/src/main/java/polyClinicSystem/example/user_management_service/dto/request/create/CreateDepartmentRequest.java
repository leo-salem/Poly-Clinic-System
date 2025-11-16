package polyClinicSystem.example.user_management_service.dto.request.create;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateDepartmentRequest {
    @NotNull(message = "Name is required")
    private String name;
    private String description;
}
