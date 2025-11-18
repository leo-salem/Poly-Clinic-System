
package polyClinicSystem.example.prescription_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polyClinicSystem.example.prescription_service.dto.response.MedicalRecordResponse;
import polyClinicSystem.example.prescription_service.service.medicalRecord.MedicalRecordService;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class MedicalRecordController {

    private final MedicalRecordService service;

    @GetMapping("/{patientId}")
    public ResponseEntity<MedicalRecordResponse> getRecord(
            @PathVariable Long patientId,
            HttpServletRequest request) throws AccessDeniedException {

        return ResponseEntity.ok(service.getRecord(patientId, request));
    }

    @PostMapping("/{patientId}")
    public ResponseEntity<MedicalRecordResponse> createOrGet(
            @PathVariable Long patientId,
            HttpServletRequest request) throws AccessDeniedException {

        MedicalRecordResponse response = service.getOrCreateRecord(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}