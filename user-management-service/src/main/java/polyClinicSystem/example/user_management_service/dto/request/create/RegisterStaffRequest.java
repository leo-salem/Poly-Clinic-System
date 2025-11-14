package polyClinicSystem.example.user_management_service.dto.request.create;

import lombok.*;
import polyClinicSystem.example.user_management_service.model.enums.BloodType;
import polyClinicSystem.example.user_management_service.model.enums.Gender;
import polyClinicSystem.example.user_management_service.model.enums.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterStaffRequest {
    private Integer age;
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private String address;
    private String Phone;
    private String username;
    private String specialization;
    private Integer  experience_years;
    private String password;
    private String confirmPassword;
    private BloodType bloodType;
    private Role role;
}
