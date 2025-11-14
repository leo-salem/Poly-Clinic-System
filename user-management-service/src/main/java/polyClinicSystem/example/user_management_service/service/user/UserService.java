package polyClinicSystem.example.user_management_service.service.user;

import polyClinicSystem.example.user_management_service.dto.request.create.RegisterStaffRequest;
import polyClinicSystem.example.user_management_service.dto.response.UserResponse;

public interface UserService {
    UserResponse registerPatient(RegisterStaffRequest request);
    UserResponse addStaff(RegisterStaffRequest request);
    void deleteUser(Long Id);
    UserResponse updateUser(Long Id, RegisterStaffRequest request);
    void ChangePassword(Long Id, String oldPassword, String newPassword);
}
