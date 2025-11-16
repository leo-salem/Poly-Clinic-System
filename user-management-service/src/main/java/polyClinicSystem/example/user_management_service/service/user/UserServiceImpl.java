package polyClinicSystem.example.user_management_service.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import polyClinicSystem.example.user_management_service.dto.request.create.RegisterStaffRequest;
import polyClinicSystem.example.user_management_service.dto.response.UserResponse;
import polyClinicSystem.example.user_management_service.exception.customExceptions.BadRequestException;
import polyClinicSystem.example.user_management_service.exception.customExceptions.KeycloakOperationException;
import polyClinicSystem.example.user_management_service.exception.customExceptions.NotFoundException;
import polyClinicSystem.example.user_management_service.mapper.MapperSystem;
import polyClinicSystem.example.user_management_service.model.department.Department;
import polyClinicSystem.example.user_management_service.model.enums.Role;
import polyClinicSystem.example.user_management_service.model.user.Doctor;
import polyClinicSystem.example.user_management_service.model.user.Nurse;
import polyClinicSystem.example.user_management_service.model.user.Patient;
import polyClinicSystem.example.user_management_service.model.user.User;
import polyClinicSystem.example.user_management_service.repository.DepartmentRepository;
import polyClinicSystem.example.user_management_service.repository.DoctorRepository;
import polyClinicSystem.example.user_management_service.repository.NurseRepository;
import polyClinicSystem.example.user_management_service.repository.PatientRepository;
import polyClinicSystem.example.user_management_service.repository.UserRepository;
import polyClinicSystem.example.user_management_service.service.keycloakAdmin.KeycloakAdminService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final MapperSystem mapperSystem;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public UserResponse registerPatient(RegisterStaffRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }

        // create token and user in Keycloak
        String token = keycloakAdminService.getAdminAccessToken();
        String keycloakId;
        try {
            keycloakId = keycloakAdminService.createUser(token, request);
        } catch (Exception e) {
            log.error("Failed to create patient in Keycloak", e);
            throw new KeycloakOperationException("Failed to create patient in Keycloak: " + e.getMessage());
        }

        // map to Patient entity, persist
        Patient patient = mapperSystem.toPatient(request);
        patient.setKeycloakID(keycloakId);
        patient.setRole(Role.PATIENT);
        Patient saved = patientRepository.save(patient);

        // assign realm role
        keycloakAdminService.assignRealmRoleToUser(keycloakId, Role.PATIENT.name());

        log.info("Patient registered successfully: {}", saved.getUsername());
        return mapperSystem.toUserResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse addStaff(RegisterStaffRequest request) {
        Role role = request.getRole();
        if (role == null || role == Role.PATIENT) {
            throw new BadRequestException("Staff must have role DOCTOR or NURSE");
        }

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }

        // Validate and fetch department if provided
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new NotFoundException("Department not found with id: " + request.getDepartmentId()));
            log.debug("Assigning staff to department: {}", department.getName());
        }

        String token = keycloakAdminService.getAdminAccessToken();
        String keycloakId;
        try {
            keycloakId = keycloakAdminService.createUser(token, request);
        } catch (Exception e) {
            log.error("Failed to create staff member in Keycloak", e);
            throw new KeycloakOperationException("Failed to create staff member in Keycloak: " + e.getMessage());
        }

        // Persist locally according to role
        if (role == Role.NURSE) {
            Nurse nurse = mapperSystem.toNurse(request);
            nurse.setKeycloakID(keycloakId);
            nurse.setRole(Role.NURSE);

            // Assign to department if provided
            if (department != null) {
                nurse.setDepartment(department);
                department.AddNurse(nurse);
            }

            Nurse saved = nurseRepository.save(nurse);

            keycloakAdminService.assignRealmRoleToUser(keycloakId, Role.NURSE.name());
            log.info("Nurse created successfully: {} in department: {}",
                    saved.getUsername(),
                    department != null ? department.getName() : "none");
            return mapperSystem.toUserResponse(saved);
        } else {
            // DOCTOR
            Doctor doctor = mapperSystem.toDoctor(request);
            doctor.setKeycloakID(keycloakId);
            doctor.setRole(Role.DOCTOR);

            // Assign to department if provided
            if (department != null) {
                doctor.setDepartment(department);
                department.AddDoctor(doctor);
            }

            Doctor saved = doctorRepository.save(doctor);

            keycloakAdminService.assignRealmRoleToUser(keycloakId, Role.DOCTOR.name());
            log.info("Doctor created successfully: {} in department: {}",
                    saved.getUsername(),
                    department != null ? department.getName() : "none");
            return mapperSystem.toUserResponse(saved);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String keycloakId = user.getKeycloakID();
        if (StringUtils.hasText(keycloakId)) {
            try {
                keycloakAdminService.deleteUser(user.getKeycloakID());
            } catch (Exception e) {
                log.error("Failed deleting user from Keycloak", e);
                throw new KeycloakOperationException("Failed deleting user from Keycloak: " + e.getMessage());
            }
        }
        // Clean up bidirectional relationship

        if (user instanceof Doctor ) {
            Doctor doctor = (Doctor)user;
            if (doctor.getDepartment()!= null){
                doctor.getDepartment().RemoveDoctor(doctor);
            }
        }
        if (user instanceof Nurse ) {
            Nurse nurse = (Nurse)user;
            if (nurse.getDepartment()!= null){
                nurse.getDepartment().RemoveNurse(nurse);
            }
        }

        userRepository.delete(user);
        log.info("User deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, RegisterStaffRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        log.debug("Updating user with id: {}", id);

        boolean hasKeycloakUpdates = false;

        // Keycloak payload
        Map<String, Object> kcPayload = new HashMap<>();
        Map<String, List<String>> attrs = new HashMap<>();

        // email / names are top-level fields in Keycloak
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            kcPayload.put("email", request.getEmail());
            user.setEmail(request.getEmail());
            hasKeycloakUpdates = true;
        }
        if (StringUtils.hasText(request.getFirstName()) && !request.getFirstName().equals(user.getFirstName())) {
            kcPayload.put("firstName", request.getFirstName());
            user.setFirstName(request.getFirstName());
            hasKeycloakUpdates = true;
        }
        if (StringUtils.hasText(request.getLastName()) && !request.getLastName().equals(user.getLastName())) {
            kcPayload.put("lastName", request.getLastName());
            user.setLastName(request.getLastName());
            hasKeycloakUpdates = true;
        }

        // Optional fields
        if (request.getAge() != null && !request.getAge().equals(user.getAge())) {
            user.setAge(request.getAge());
            attrs.put("age", List.of(String.valueOf(request.getAge())));
            hasKeycloakUpdates = true;
        }

        if (request.getGender() != null && request.getGender() != user.getGender()) {
            user.setGender(request.getGender());
            attrs.put("gender", List.of(request.getGender().name()));
            hasKeycloakUpdates = true;
        }

        if (StringUtils.hasText(request.getAddress()) && !request.getAddress().equals(user.getAddress())) {
            user.setAddress(request.getAddress());
            attrs.put("address", List.of(request.getAddress()));
            hasKeycloakUpdates = true;
        }

        if (StringUtils.hasText(request.getPhone()) && !request.getPhone().equals(user.getPhone())) {
            user.setPhone(request.getPhone());
            attrs.put("phone", List.of(request.getPhone()));
            hasKeycloakUpdates = true;
        }

        // Role specific updates
        if (user instanceof Doctor) {
            Doctor doc = (Doctor) user;

            if (StringUtils.hasText(request.getSpecialization()) &&
                    !request.getSpecialization().equals(doc.getSpecialization())) {
                doc.setSpecialization(request.getSpecialization());
                attrs.put("specialization", List.of(request.getSpecialization()));
                hasKeycloakUpdates = true;
            }

            if (request.getExperience_years() != null &&
                    !request.getExperience_years().equals(doc.getExperience_years())) {
                doc.setExperience_years(request.getExperience_years());
                attrs.put("experience_years", List.of(String.valueOf(request.getExperience_years())));
                hasKeycloakUpdates = true;
            }

            // Update department if provided
            if (request.getDepartmentId() != null) {
                Department newDepartment = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new NotFoundException("Department not found with id: " + request.getDepartmentId()));

                // Remove from old department
                if (doc.getDepartment() != null) {
                    doc.getDepartment().RemoveDoctor(doc);
                }

                // Add to new department
                doc.setDepartment(newDepartment);
                newDepartment.AddDoctor(doc);
            }

            doctorRepository.save(doc);

        } else if (user instanceof Nurse) {
            Nurse nurse = (Nurse) user;

            // Update department if provided
            if (request.getDepartmentId() != null) {
                Department newDepartment = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new NotFoundException("Department not found with id: " + request.getDepartmentId()));

                // Remove from old department
                if (nurse.getDepartment() != null) {
                    nurse.getDepartment().RemoveNurse(nurse);
                }

                // Add to new department
                nurse.setDepartment(newDepartment);
                newDepartment.AddNurse(nurse);
            }

            nurseRepository.save(nurse);

        } else if (user instanceof Patient) {
            Patient patient = (Patient) user;

            if (request.getBloodType() != null && request.getBloodType() != patient.getBloodType()) {
                patient.setBloodType(request.getBloodType());
                attrs.put("bloodType", List.of(request.getBloodType().name()));
                hasKeycloakUpdates = true;
            }

            patientRepository.save(patient);
        } else {
            userRepository.save(user);
        }

        if (!attrs.isEmpty()) {
            kcPayload.put("attributes", attrs);
        }

        String kcId = user.getKeycloakID();
        if (hasKeycloakUpdates && StringUtils.hasText(kcId)) {
            try {
                log.debug("Updating user in Keycloak with payload: {}", kcPayload);
                keycloakAdminService.updateUser(kcId, kcPayload);
            } catch (Exception e) {
                log.error("Failed updating user in Keycloak: {}", e.getMessage(), e);
                throw new KeycloakOperationException("Failed updating user in Keycloak: " + e.getMessage());
            }
        }

        // refresh and return
        User updated = userRepository.findById(id).orElseThrow();
        log.info("User updated successfully: {}", id);

        if (updated instanceof Doctor) return mapperSystem.toUserResponse((Doctor) updated);
        if (updated instanceof Nurse) return mapperSystem.toUserResponse((Nurse) updated);
        return mapperSystem.toUserResponse((Patient) updated);
    }

    @Override
    @Transactional
    public void ChangePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String keycloakId = user.getKeycloakID();
        if (!StringUtils.hasText(keycloakId)) {
            throw new KeycloakOperationException("User has no Keycloak id");
        }

        try {
            keycloakAdminService.updateUserPassword(keycloakId, newPassword);
            log.info("Password changed successfully for user: {}", id);
        } catch (Exception e) {
            log.error("Failed changing user password in Keycloak", e);
            throw new KeycloakOperationException("Failed changing user password in Keycloak: " + e.getMessage());
        }
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(mapperSystem::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        return mapperSystem.toUserResponse(
                userRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("User not found with id: " + id)));
    }
}