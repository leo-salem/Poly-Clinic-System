package polyClinicSystem.example.user_management_service.service.department;

import polyClinicSystem.example.user_management_service.dto.request.create.CreateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.request.update.UpdateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse createDepartment(CreateDepartmentRequest request);
    DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request);
    void deleteDepartment(Long id);
    DepartmentResponse getDepartment(Long id);
    List<DepartmentResponse> getAllDepartments();
}
