package polyClinicSystem.example.user_management_service.service.department;

import polyClinicSystem.example.user_management_service.dto.request.create.CreateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.request.update.UpdateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.response.DepartmentResponse;
import polyClinicSystem.example.user_management_service.dto.response.RoomResponse;
import polyClinicSystem.example.user_management_service.dto.response.UserResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request);

    void deleteDepartment(Long id);

    DepartmentResponse getDepartment(Long id);

    List<DepartmentResponse> getAllDepartments();

    List<UserResponse> getDoctorsByDepartment(Long departmentId);

    List<UserResponse> getNursesByDepartment(Long departmentId);

    List<UserResponse> getAllStaffByDepartment(Long departmentId);

    List<RoomResponse> getRoomsByDepartment(Long departmentId);
}