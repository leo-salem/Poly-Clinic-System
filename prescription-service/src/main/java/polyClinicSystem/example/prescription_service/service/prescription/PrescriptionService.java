package polyClinicSystem.example.prescription_service.service.prescription;

import jakarta.servlet.http.HttpServletRequest;
import polyClinicSystem.example.prescription_service.dto.request.CreatePrescription;
import polyClinicSystem.example.prescription_service.dto.request.UpdatePrescription;
import polyClinicSystem.example.prescription_service.dto.response.PrescriptionResponse;

import java.nio.file.AccessDeniedException;

public interface PrescriptionService {

    PrescriptionResponse createPrescription(CreatePrescription request, HttpServletRequest httpRequest) throws AccessDeniedException;

    PrescriptionResponse updatePrescription(Long id, UpdatePrescription request, HttpServletRequest httpRequest) throws AccessDeniedException;

    void deletePrescription(Long id, HttpServletRequest httpRequest) throws AccessDeniedException;

    PrescriptionResponse getPrescription(Long id, HttpServletRequest httpRequest) throws AccessDeniedException;

}
