package polyClinicSystem.example.prescription_service.service.medicalRecord;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import polyClinicSystem.example.prescription_service.client.UserClient;
import polyClinicSystem.example.prescription_service.dto.response.MedicalRecordResponse;
import polyClinicSystem.example.prescription_service.dto.response.UserResponse;
import polyClinicSystem.example.prescription_service.exception.customExceptions.AccessDeniedException;
import polyClinicSystem.example.prescription_service.exception.customExceptions.NotFoundException;
import polyClinicSystem.example.prescription_service.mapper.MapperSystem;
import polyClinicSystem.example.prescription_service.model.MedicalRecord;
import polyClinicSystem.example.prescription_service.repository.MedicalRecordRepository;
import polyClinicSystem.example.prescription_service.service.token.TokenService;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordImpl implements MedicalRecordService {

    private final MedicalRecordRepository repository;
    private final MapperSystem mapper;
    private final UserClient userClient;
    private final TokenService tokenService;

    /**
     * Helper method to get current authenticated user
     */
    private UserResponse getCurrentUser(HttpServletRequest request) {
        String userId = tokenService.extractUserId(request);
        log.debug("Fetching current user with ID: {}", userId);

        try {
            return userClient.getUserById(userId);
        } catch (Exception e) {
            log.error("Failed to fetch user with ID: {}", userId, e);
            throw new NotFoundException("User not found with id: " + userId);
        }
    }


    //  Validate that user can access medical record (doctor or the patient themselves)

    private void validateAccess(UserResponse currentUser, Long patientId) {
        // Convert String ID to Long for comparison
        Long currentUserId = currentUser.getId();

        boolean isDoctor = "DOCTOR".equals(currentUser.getRole());
        boolean isOwner = currentUserId.equals(patientId);

        if (!isDoctor && !isOwner) {
            log.warn("User {} denied access to medical record of patient {}",
                    currentUser.getId(), patientId);
            throw new AccessDeniedException("You do not have permission to access this medical record");
        }
    }

    @Override
    @Transactional
    public MedicalRecordResponse getOrCreateRecord(Long patientId, HttpServletRequest request) {
        log.debug("Getting or creating medical record for patient: {}", patientId);

        UserResponse currentUser = getCurrentUser(request);
        validateAccess(currentUser, patientId);

        // Try to find existing record
        return repository.findByPatientId(patientId)
                .map(record -> {
                    log.info("Found existing medical record: {} for patient: {}", record.getId(), patientId);
                    return mapper.toMedicalRecordResponse(record);
                })
                .orElseGet(() -> {
                    log.info("Creating new medical record for patient: {}", patientId);

                    MedicalRecord record = MedicalRecord.builder()
                            .patientId(patientId)
                            .build();

                    MedicalRecord saved = repository.save(record);

                    log.info("Medical record created successfully: {} for patient: {}",
                            saved.getId(), patientId);

                    return mapper.toMedicalRecordResponse(saved);
                });
    }

    @Override
    public MedicalRecordResponse getRecord(Long patientId, HttpServletRequest request) {
        log.debug("Fetching medical record for patient: {}", patientId);

        UserResponse currentUser = getCurrentUser(request);
        validateAccess(currentUser, patientId);

        MedicalRecord record = repository.findByPatientId(patientId)
                .orElseThrow(() -> new NotFoundException("Medical record not found for patient: " + patientId));

        log.info("Medical record fetched successfully: {} for patient: {}", record.getId(), patientId);

        return mapper.toMedicalRecordResponse(record);
    }
}