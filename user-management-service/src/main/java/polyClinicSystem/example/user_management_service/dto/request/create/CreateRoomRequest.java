package polyClinicSystem.example.user_management_service.dto.request.create;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRoomRequest {
    private Long roomNumber;
    private String type;
    private Long departmentId;
}
