package polyClinicSystem.example.user_management_service.dto.request.create;


import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRoomRequest {
    @NotNull(message = "Room number is required")
    private Long roomNumber;
    private String type;
    @NotNull(message = "Department ID is required")
    private Long departmentId;
}
