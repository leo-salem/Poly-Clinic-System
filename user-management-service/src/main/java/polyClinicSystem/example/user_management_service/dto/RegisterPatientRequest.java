package polyClinicSystem.example.user_management_service.dto;

import lombok.*;
import polyClinicSystem.example.user_management_service.model.enums.BloodType;
import polyClinicSystem.example.user_management_service.model.enums.Gender;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterPatientRequest {
    private int age;
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private String address;
    private String username;
    private String password;
    private String confirmPassword;
    private BloodType bloodType;
}
