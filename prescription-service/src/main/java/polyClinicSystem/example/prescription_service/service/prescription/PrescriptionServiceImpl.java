package polyClinicSystem.example.prescription_service.service.prescription;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import polyClinicSystem.example.prescription_service.client.UserClient;
import polyClinicSystem.example.prescription_service.dto.request.CreatePrescription;
import polyClinicSystem.example.prescription_service.dto.request.UpdatePrescription;
import polyClinicSystem.example.prescription_service.dto.response.MedicalRecordResponse;
import polyClinicSystem.example.prescription_service.dto.response.PrescriptionResponse;
import polyClinicSystem.example.prescription_service.dto.response.UserResponse;
import polyClinicSystem.example.prescription_service.exception.customExceptions.AccessDeniedException;
import polyClinicSystem.example.prescription_service.exception.customExceptions.NotFoundException;
import polyClinicSystem.example.prescription_service.mapper.MapperSystem;
import polyClinicSystem.example.prescription_service.model.MedicalRecord;
import polyClinicSystem.example.prescription_service.model.Prescription;
import polyClinicSystem.example.prescription_service.repository.MedicalRecordRepository;
import polyClinicSystem.example.prescription_service.repository.PrescriptionRepository;
import polyClinicSystem.example.prescription_service.service.medicalRecord.MedicalRecordService;
import polyClinicSystem.example.prescription_service.service.token.TokenService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionServiceImpl implements PrescriptionService {

    private final MapperSystem mapper;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordService medicalRecordService;
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

    /**
     * Validate that user is a doctor
     */
    private void validateDoctor(UserResponse user) {
        if (!"DOCTOR".equals(user.getRole())) {
            log.warn("Non-doctor user {} attempted to perform doctor-only action", user.getId());
            throw new AccessDeniedException("Only doctors can perform this action");
        }
    }

    /**
     * Validate that user can access prescription (either the doctor who created it or the patient)
     */
    private void validatePrescriptionAccess(UserResponse user, Prescription prescription) {
        boolean isDoctor = "DOCTOR".equals(user.getRole()) &&
                prescription.getDoctorId().equals(Long.valueOf(user.getId()));
        boolean isPatient = prescription.getPatientId().equals(user.getId());

        if (!isDoctor && !isPatient) {
            log.warn("User {} denied access to prescription {}", user.getId(), prescription.getId());
            throw new AccessDeniedException("You do not have permission to access this prescription");
        }
    }

    @Override
    @Transactional
    public PrescriptionResponse createPrescription(CreatePrescription request, HttpServletRequest httpRequest) throws java.nio.file.AccessDeniedException {
        log.debug("Creating prescription for patient: {}", request.getPatientId());

        // Validate user is a doctor
        UserResponse doctor = getCurrentUser(httpRequest);
        validateDoctor(doctor);
        // Fetch medical record
        MedicalRecord record = medicalRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new NotFoundException("Medical record not found with id: " + request.getRecordId()));

        // Verify the medical record belongs to the specified patient
        if (!record.getPatientId().equals(request.getPatientId())) {
            log.error("Medical record {} does not belong to patient {}",
                    request.getRecordId(), request.getPatientId());
            throw new IllegalArgumentException("Medical record does not belong to the specified patient");
        }

        // Create prescription
        Prescription prescription = mapper.toPrescription(request);
        prescription.setDoctorId(doctor.getId());
        prescription.setPatientId(request.getPatientId());
        prescription.setMedicalRecord(record);

        // Add to medical record using bidirectional helper
        record.AddPrescription(prescription);

        Prescription saved = prescriptionRepository.save(prescription);

        log.info("Prescription created successfully - ID: {}, Doctor: {}, Patient: {}",
                saved.getId(), doctor.getId(), request.getPatientId());

        return mapper.toPrescriptionResponse(saved);
    }

    @Override
    @Transactional
    public PrescriptionResponse updatePrescription(Long id, UpdatePrescription request, HttpServletRequest httpRequest) {
        log.debug("Updating prescription: {}", id);

        // Fetch prescription
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found with id: " + id));

        // Validate user is the doctor who created it
        UserResponse doctor = getCurrentUser(httpRequest);
        validateDoctor(doctor);

        if (!prescription.getDoctorId().equals(doctor.getId())) {
            log.warn("Doctor {} attempted to update prescription {} created by doctor {}",
                    doctor.getId(), id, prescription.getDoctorId());
            throw new AccessDeniedException("Only the doctor who created this prescription can update it");
        }

        // Update fields
        if (request.getDiagnose() != null) {
            prescription.setDiagnose(request.getDiagnose());
        }
        if (request.getMedicine() != null) {
            prescription.setMedicine(request.getMedicine());
        }

        Prescription saved = prescriptionRepository.save(prescription);

        log.info("Prescription updated successfully: {}", id);

        return mapper.toPrescriptionResponse(saved);
    }

    @Override
    @Transactional
    public void deletePrescription(Long id, HttpServletRequest httpRequest) {
        log.debug("Deleting prescription: {}", id);

        // Fetch prescription
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found with id: " + id));

        // Validate user is the doctor who created it
        UserResponse doctor = getCurrentUser(httpRequest);
        validateDoctor(doctor);

        if (!prescription.getDoctorId().equals(doctor.getId())) {
            log.warn("Doctor {} attempted to delete prescription {} created by doctor {}",
                    doctor.getId(), id, prescription.getDoctorId());
            throw new AccessDeniedException("Only the doctor who created this prescription can delete it");
        }

        // Remove from medical record using bidirectional helper
        if (prescription.getMedicalRecord() != null) {
            prescription.getMedicalRecord().RemovePrescription(prescription);
        }

        prescriptionRepository.delete(prescription);

        log.info("Prescription deleted successfully: {}", id);
    }

    @Override
    public PrescriptionResponse getPrescription(Long id, HttpServletRequest httpRequest) {
        log.debug("Fetching prescription: {}", id);

        // Fetch prescription
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found with id: " + id));

        // Validate access (doctor who created it OR the patient)
        UserResponse user = getCurrentUser(httpRequest);
        validatePrescriptionAccess(user, prescription);

        log.info("Prescription fetched successfully: {} by user: {}", id, user.getId());

        return mapper.toPrescriptionResponse(prescription);
    }
}