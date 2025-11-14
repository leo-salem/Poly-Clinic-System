package polyClinicSystem.example.user_management_service.dto.request.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import polyClinicSystem.example.user_management_service.model.enums.BloodType;
import polyClinicSystem.example.user_management_service.model.enums.Gender;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Integer age;
    private Gender gender;
    private String Phone;
    private String address;
    private String specialization;
    private Integer experience_years;
    private BloodType bloodType;
}
