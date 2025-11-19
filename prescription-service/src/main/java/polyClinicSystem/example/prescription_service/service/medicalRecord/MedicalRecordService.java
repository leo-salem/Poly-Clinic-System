package polyClinicSystem.example.prescription_service.service.medicalRecord;

import jakarta.servlet.http.HttpServletRequest;
import polyClinicSystem.example.prescription_service.dto.response.MedicalRecordResponse;

public interface MedicalRecordService {
     MedicalRecordResponse getOrCreateRecord(String userId, HttpServletRequest request) ;
    MedicalRecordResponse getRecord(String userId, HttpServletRequest request) ;
}

