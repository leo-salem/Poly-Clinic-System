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
    private String phone;
    private String username;

    // For doctors and nurses
    private String specialization;
    private Integer experience_years;
    private Long departmentId;  // Department assignment for staff

    // For patients
    private BloodType bloodType;

    // Authentication
    private String password;
    private String confirmPassword;

    // Role
    private Role role;
}