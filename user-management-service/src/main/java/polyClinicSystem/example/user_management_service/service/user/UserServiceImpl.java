package polyClinicSystem.example.user_management_service.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import polyClinicSystem.example.user_management_service.dto.request.create.RegisterStaffRequest;
import polyClinicSystem.example.user_management_service.dto.response.UserResponse;
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
import java.util.Map;
import java.util.Optional;

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
        String keycloakId = keycloakAdminService.createUser(token, request);

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
            throw new IllegalArgumentException("Staff must have role DOCTOR or NURSE");
        }

        String token = keycloakAdminService.getAdminAccessToken();
        String keycloakId = keycloakAdminService.createUser(token, request);

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
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("deleteUser: user id {} not found", id);
            return; // or throw custom NotFoundException
        }
        User user = opt.get();

        String keycloakId = user.getKeycloakID();
        if (StringUtils.hasText(keycloakId)) {
            try {
                keycloakAdminService.deleteUser(keycloakId);
            } catch (Exception e) {
                log.error("Failed to delete user {} in Keycloak: {}", keycloakId, e.getMessage());
                // Decide policy: continue deleting DB record or abort. Here continue local delete.
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
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        // Build payload for Keycloak update (only include fields that exist in the request)
        Map<String, Object> kcPayload = new HashMap<>();

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
        if (request.getAge() != 0 && request.getAge() != user.getAge()) {
            user.setAge(request.getAge());
            // age is not a Keycloak standard claim — if you want it in KC, use attributes
            kcPayload.computeIfAbsent("attributes", k -> new HashMap<String, Object>());
            @SuppressWarnings("unchecked")
            Map<String, Object> attrs = (Map<String, Object>) kcPayload.get("attributes");
            attrs.put("age", String.valueOf(request.getAge()));
        }
        if (request.getGender() != null && request.getGender() != user.getGender()) {
            user.setGender(request.getGender());
            kcPayload.computeIfAbsent("attributes", k -> new HashMap<String, Object>());
            @SuppressWarnings("unchecked")
            Map<String, Object> attrs = (Map<String, Object>) kcPayload.get("attributes");
            attrs.put("gender", request.getGender().name());
        }
        if (StringUtils.hasText(request.getAddress()) && !request.getAddress().equals(user.getAddress())) {
            user.setAddress(request.getAddress());
            kcPayload.computeIfAbsent("attributes", k -> new HashMap<String, Object>());
            @SuppressWarnings("unchecked")
            Map<String, Object> attrsAddr = (Map<String, Object>) kcPayload.get("attributes");
            attrsAddr.put("address", request.getAddress());
        }
        if (StringUtils.hasText(request.getPhone()) && !request.getPhone().equals(user.getPhone())) {
            user.setAddress(request.getPhone());
            kcPayload.computeIfAbsent("attributes", k -> new HashMap<String, Object>());
            @SuppressWarnings("unchecked")
            Map<String, Object> attrs = (Map<String, Object>) kcPayload.get("attributes");
            attrs.put("phone", request.getPhone());
        }

        // For staff-specific fields (specialization, experience, department, bloodType) map accordingly
        if (user instanceof Doctor) {
            Doctor doc = (Doctor) user;
            if (StringUtils.hasText(request.getSpecialization()) && !request.getSpecialization().equals(doc.getSpecialization())) {
                doc.setSpecialization(request.getSpecialization());
            }
            if (request.getExperience_years() != 0 && request.getExperience_years() != doc.getExperience_years()) {
                doc.setExperience_years(request.getExperience_years());
            }
            // department handling: prefer a service call to set relationship by id (not here)
            doctorRepository.save(doc);
        } else if (user instanceof Nurse) {
            Nurse nurse = (Nurse) user;
            // department update similar note as above
            nurseRepository.save(nurse);
        } else if (user instanceof Patient) {
            Patient patient = (Patient) user;
            if (request.getBloodType() != null && request.getBloodType() != patient.getBloodType()) {
                patient.setBloodType(request.getBloodType());
            }
            patientRepository.save(patient);
        } else {
            // Generic user update
            userRepository.save(user);
        }

        // Update Keycloak if there is anything to update
        String kcId = user.getKeycloakID();
        if (!kcPayload.isEmpty() && StringUtils.hasText(kcId)) {
            keycloakAdminService.updateUser(kcId, kcPayload);
        }

        // Return updated DTO
        // fetch fresh entity to ensure we return saved state
        User updated = userRepository.findById(id).orElseThrow();
        if (updated instanceof Doctor) return mapperSystem.toUserResponse((Doctor) updated);
        if (updated instanceof Nurse) return mapperSystem.toUserResponse((Nurse) updated);
        return mapperSystem.toUserResponse((Patient) updated);
    }


    //  Change password using Keycloak reset-password API (admin endpoint).

    @Override
    public void ChangePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String keycloakId = user.getKeycloakID();
        if (!StringUtils.hasText(keycloakId)) throw new IllegalStateException("User has no Keycloak id");
        keycloakAdminService.updateUserPassword(keycloakId, newPassword);
    }
}
