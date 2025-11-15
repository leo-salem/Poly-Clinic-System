package polyClinicSystem.example.user_management_service.service.department;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import polyClinicSystem.example.user_management_service.dto.request.create.CreateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.request.update.UpdateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.response.DepartmentResponse;
import polyClinicSystem.example.user_management_service.exception.customExceptions.NotFoundException;
import polyClinicSystem.example.user_management_service.mapper.MapperSystem;
import polyClinicSystem.example.user_management_service.model.department.Department;
import polyClinicSystem.example.user_management_service.repository.DepartmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final MapperSystem mapperSystem;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        Department department = mapperSystem.toDepartment(request);
        departmentRepository.save(department);
        return mapperSystem.toDepartmentResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));

        if (StringUtils.hasText(request.getName()))
            department.setName(request.getName());

        if (StringUtils.hasText(request.getDescription()))
            department.setDescription(request.getDescription());

        departmentRepository.save(department);

        return mapperSystem.toDepartmentResponse(department);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }

    @Override
    public DepartmentResponse getDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));
        return mapperSystem.toDepartmentResponse(department);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(mapperSystem::toDepartmentResponse)
                .toList();
    }
}
