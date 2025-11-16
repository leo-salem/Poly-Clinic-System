package polyClinicSystem.example.user_management_service.service.department;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import polyClinicSystem.example.user_management_service.dto.request.create.CreateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.request.update.UpdateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.response.DepartmentResponse;
import polyClinicSystem.example.user_management_service.dto.response.RoomResponse;
import polyClinicSystem.example.user_management_service.dto.response.UserResponse;
import polyClinicSystem.example.user_management_service.exception.customExceptions.NotFoundException;
import polyClinicSystem.example.user_management_service.mapper.MapperSystem;
import polyClinicSystem.example.user_management_service.model.department.Department;
import polyClinicSystem.example.user_management_service.repository.DepartmentRepository;
import polyClinicSystem.example.user_management_service.repository.DoctorRepository;
import polyClinicSystem.example.user_management_service.repository.NurseRepository;
import polyClinicSystem.example.user_management_service.repository.RoomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final MapperSystem mapperSystem;
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        log.debug("Creating department with name: {}", request.getName());

        Department department = mapperSystem.toDepartment(request);
        Department saved = departmentRepository.save(department);

        log.info("Department created successfully with id: {}", saved.getId());

        return mapperSystem.toDepartmentResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, UpdateDepartmentRequest request) {
        log.debug("Updating department: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + id));

        if (StringUtils.hasText(request.getName())) {
            department.setName(request.getName());
        }

        if (StringUtils.hasText(request.getDescription())) {
            department.setDescription(request.getDescription());
        }

        Department saved = departmentRepository.save(department);

        log.info("Department updated successfully: {}", id);

        return mapperSystem.toDepartmentResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        log.debug("Deleting department: {}", id);

        if (!departmentRepository.existsById(id)) {
            throw new NotFoundException("Department not found with id: " + id);
        }

        departmentRepository.deleteById(id);

        log.info("Department deleted successfully: {}", id);
    }

    @Override
    public DepartmentResponse getDepartment(Long id) {
        log.debug("Fetching department: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + id));

        return mapperSystem.toDepartmentResponse(department);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        log.debug("Fetching all departments");

        List<DepartmentResponse> departments = departmentRepository.findAll()
                .stream()
                .map(mapperSystem::toDepartmentResponse)
                .toList();

        log.debug("Found {} departments", departments.size());

        return departments;
    }

    @Override
    public List<UserResponse> getDoctorsByDepartment(Long departmentId) {
        log.debug("Fetching doctors for department: {}", departmentId);

        // Verify department exists
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + departmentId));

        List<UserResponse> doctors = doctorRepository.findAll()
                .stream()
                .filter(doctor -> doctor.getDepartment() != null &&
                        doctor.getDepartment().getId().equals(departmentId))
                .map(mapperSystem::toUserResponse)
                .collect(Collectors.toList());

        log.debug("Found {} doctors in department {}", doctors.size(), departmentId);

        return doctors;
    }

    @Override
    public List<UserResponse> getNursesByDepartment(Long departmentId) {
        log.debug("Fetching nurses for department: {}", departmentId);

        // Verify department exists
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + departmentId));

        List<UserResponse> nurses = nurseRepository.findAll()
                .stream()
                .filter(nurse -> nurse.getDepartment() != null &&
                        nurse.getDepartment().getId().equals(departmentId))
                .map(mapperSystem::toUserResponse)
                .collect(Collectors.toList());

        log.debug("Found {} nurses in department {}", nurses.size(), departmentId);

        return nurses;
    }
    @Override
    public List<RoomResponse> getRoomsByDepartment(Long departmentId) {
        log.debug("Fetching nurses for department: {}", departmentId);

        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + departmentId));

        List<RoomResponse> rooms = roomRepository.findAll()
                .stream()
                .filter(room -> room.getDepartment() != null &&
                        room.getDepartment().getId().equals(departmentId))
                .map(mapperSystem::toRoomResponse)
                .collect(Collectors.toList());

        log.debug("Found {} nurses in department {}", rooms.size(), departmentId);

        return rooms;
    }

    @Override
    public List<UserResponse> getAllStaffByDepartment(Long departmentId) {
        log.debug("Fetching all staff for department: {}", departmentId);

        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + departmentId));

        List<UserResponse> allStaff = new ArrayList<>();

        // Add all doctors
        allStaff.addAll(getDoctorsByDepartment(departmentId));

        // Add all nurses
        allStaff.addAll(getNursesByDepartment(departmentId));

        log.debug("Found {} total staff members in department {}", allStaff.size(), departmentId);

        return allStaff;
    }
}