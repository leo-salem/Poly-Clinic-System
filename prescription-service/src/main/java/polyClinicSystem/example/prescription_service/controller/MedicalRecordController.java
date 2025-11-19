
package polyClinicSystem.example.prescription_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polyClinicSystem.example.prescription_service.dto.response.MedicalRecordResponse;
import polyClinicSystem.example.prescription_service.exception.customExceptions.AccessDeniedException;
import polyClinicSystem.example.prescription_service.service.medicalRecord.MedicalRecordService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class MedicalRecordController {

    private final MedicalRecordService service;

    @PostMapping("/{userId}")
    public ResponseEntity<MedicalRecordResponse> createOrGet(
            @PathVariable String userId,
            HttpServletRequest request) throws AccessDeniedException {

        MedicalRecordResponse response = service.getOrCreateRecord(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}