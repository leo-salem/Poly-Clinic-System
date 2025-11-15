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
import polyClinicSystem.example.user_management_service.model.enums.Role;
import polyClinicSystem.example.user_management_service.model.user.Doctor;
import polyClinicSystem.example.user_management_service.model.user.Nurse;
import polyClinicSystem.example.user_management_service.model.user.Patient;
import polyClinicSystem.example.user_management_service.model.user.User;
import polyClinicSystem.example.user_management_service.repository.DoctorRepository;
import polyClinicSystem.example.user_management_service.repository.NurseRepository;
import polyClinicSystem.example.user_management_service.repository.PatientRepository;
import polyClinicSystem.example.user_management_service.repository.UserRepository;
import polyClinicSystem.example.user_management_service.service.keycloakAdmin.KeycloakAdminService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    /**
     * Register a patient (self-registration or public endpoint).
     * Creates user in Keycloak, assigns realm role PATIENT, then persists local Patient entity.
     */
    @Override
    @Transactional
    public UserResponse registerPatient(RegisterStaffRequest request) {
        // create token and user in Keycloak
        String token = keycloakAdminService.getAdminAccessToken();
        String keycloakId;
        try {
            keycloakId = keycloakAdminService.createUser(token, request);
            if(!request.getPassword().equals(request.getConfirmPassword())) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new KeycloakOperationException("Failed to create patient in Keycloak");
        }

        // map to Patient entity, persist
        Patient patient = mapperSystem.toPatient(request);
        patient.setKeycloakID(keycloakId);
        patient.setRole(Role.PATIENT);
        Patient saved = patientRepository.save(patient);

        // assign realm role
        keycloakAdminService.assignRealmRoleToUser(keycloakId, Role.PATIENT.name());

        return mapperSystem.toUserResponse(saved);
    }

    /**
     * Add staff (Doctor or Nurse). Only ADMIN / USER_ADMIN should call this method.
     * Creates user in Keycloak, assigns realm role, then persist staff-specific entity.
     */
    @Override
    @Transactional
    public UserResponse addStaff(RegisterStaffRequest request) {
        Role role = request.getRole();
        if (role == null || role == Role.PATIENT) {
            throw new BadRequestException("Staff must have role DOCTOR or NURSE");
        }

        String token = keycloakAdminService.getAdminAccessToken();
        String keycloakId;
        try {
            keycloakId = keycloakAdminService.createUser(token, request);
            if(!request.getPassword().equals(request.getConfirmPassword())) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new KeycloakOperationException("Failed to create staff member in Keycloak");
        }

        // Persist locally according to role
        if (role == Role.NURSE) {
            Nurse nurse = mapperSystem.toNurse(request);
            nurse.setKeycloakID(keycloakId);
            nurse.setRole(Role.NURSE);
            Nurse saved = nurseRepository.save(nurse);

            keycloakAdminService.assignRealmRoleToUser(keycloakId, Role.NURSE.name());
            return mapperSystem.toUserResponse(saved);
        } else {
            // DOCTOR
            Doctor doctor = mapperSystem.toDoctor(request);
            doctor.setKeycloakID(keycloakId);
            doctor.setRole(Role.DOCTOR);
            Doctor saved = doctorRepository.save(doctor);

            keycloakAdminService.assignRealmRoleToUser(keycloakId, Role.DOCTOR.name());
            return mapperSystem.toUserResponse(saved);
        }
    }

    /**
     * Delete user both from Keycloak (by keycloak id) and local DB.
     */
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
                throw new KeycloakOperationException("Failed deleting user from Keycloak");
            }
        }
        userRepository.delete(user);
    }

    /**
     * Update user (partial). Only non-null / non-empty fields in request will be updated.
     * This updates both Keycloak representation (username/email/firstName/lastName) and local DB entity.
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long id, RegisterStaffRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Keycloak payload
        Map<String, Object> kcPayload = new HashMap<>();
        // attributes expected as Map<String, List<String>> by Keycloak
        Map<String, List<String>> attrs = new HashMap<>();

        try {
            // Username / email / names are top-level fields in Keycloak
            if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
                kcPayload.put("username", request.getUsername());
                user.setUsername(request.getUsername());
            }
            if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
                kcPayload.put("email", request.getEmail());
                user.setEmail(request.getEmail());
            }
            if (StringUtils.hasText(request.getFirstName()) && !request.getFirstName().equals(user.getFirstName())) {
                kcPayload.put("firstName", request.getFirstName());
                user.setFirstName(request.getFirstName());
            }
            if (StringUtils.hasText(request.getLastName()) && !request.getLastName().equals(user.getLastName())) {
                kcPayload.put("lastName", request.getLastName());
                user.setLastName(request.getLastName());
            }

            // Optional numeric fields -> use Integer in DTO
            if (request.getAge() != null && !request.getAge().equals(user.getAge())) {
                user.setAge(request.getAge());
                attrs.put("age", List.of(String.valueOf(request.getAge())));
            }

            // Gender
            if (request.getGender() != null && request.getGender() != user.getGender()) {
                user.setGender(request.getGender());
                attrs.put("gender", List.of(request.getGender().name()));
            }

            // Address
            if (StringUtils.hasText(request.getAddress()) && !request.getAddress().equals(user.getAddress())) {
                user.setAddress(request.getAddress());
                attrs.put("address", List.of(request.getAddress()));
            }

            // Phone
            if (StringUtils.hasText(request.getPhone())) {
                user.setPhone(request.getPhone());
                attrs.put("phone", List.of(request.getPhone()));
            }

            // Staff specific (Doctor)
            if (user instanceof Doctor) {
                Doctor doc = (Doctor) user;
                if (StringUtils.hasText(request.getSpecialization()) && !request.getSpecialization().equals(doc.getSpecialization())) {
                    doc.setSpecialization(request.getSpecialization());
                    attrs.put("specialization", List.of(request.getSpecialization()));
                }
                if (request.getExperience_years() != null && !request.getExperience_years().equals(doc.getExperience_years())) {
                    doc.setExperience_years(request.getExperience_years());
                    attrs.put("experience_years", List.of(String.valueOf(request.getExperience_years())));
                }
                doctorRepository.save(doc);
            } else if (user instanceof Nurse) {
                Nurse nurse = (Nurse) user;
                nurseRepository.save(nurse);
            } else if (user instanceof Patient) {
                Patient patient = (Patient) user;
                if (request.getBloodType() != null && request.getBloodType() != patient.getBloodType()) {
                    patient.setBloodType(request.getBloodType());
                    attrs.put("bloodType", List.of(request.getBloodType().name()));
                }
                patientRepository.save(patient);
            } else {
                userRepository.save(user);
            }
        } catch (Exception e) {
            throw new BadRequestException("Invalid update request");
        }

        if (!attrs.isEmpty()) {
            // Keycloak expects attributes as Map<String, List<String>>
            kcPayload.put("attributes", attrs);
        }

        String kcId = user.getKeycloakID();
        if (!kcPayload.isEmpty() && StringUtils.hasText(kcId)) {
            try {
                keycloakAdminService.updateUser(user.getKeycloakID(), kcPayload);
            } catch (Exception e) {
                throw new KeycloakOperationException("Failed updating user in Keycloak");
            }
        }

        // refresh and return
        User updated = userRepository.findById(id).orElseThrow();
        if (updated instanceof Doctor) return mapperSystem.toUserResponse((Doctor) updated);
        if (updated instanceof Nurse) return mapperSystem.toUserResponse((Nurse) updated);
        return mapperSystem.toUserResponse((Patient) updated);
    }


    //  Change password using Keycloak reset-password API (admin endpoint).

    @Override
    public void ChangePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        String keycloakId = user.getKeycloakID();
        if (!StringUtils.hasText(keycloakId)) throw new KeycloakOperationException("User has no Keycloak id");
        try {
            keycloakAdminService.updateUserPassword(keycloakId, newPassword);
        } catch (Exception e) {
            throw new KeycloakOperationException("Failed Changing user password in Keycloak");
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
    public UserResponse getUserById(Long Id) {
        return mapperSystem.toUserResponse(
                userRepository.findById(Id)
                        .orElseThrow(() -> new NotFoundException("User not found")));
    }
}
