package polyClinicSystem.example.prescription_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import polyClinicSystem.example.prescription_service.model.Role;


import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private Role role;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
