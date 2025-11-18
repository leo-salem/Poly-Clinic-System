package polyClinicSystem.example.prescription_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polyClinicSystem.example.prescription_service.dto.request.CreatePrescription;
import polyClinicSystem.example.prescription_service.dto.request.UpdatePrescription;
import polyClinicSystem.example.prescription_service.dto.response.PrescriptionResponse;
import polyClinicSystem.example.prescription_service.service.prescription.PrescriptionService;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService service;

    @PostMapping
    public ResponseEntity<PrescriptionResponse> create(
            @Valid @RequestBody CreatePrescription request,
            HttpServletRequest httpRequest) throws AccessDeniedException {

        PrescriptionResponse response = service.createPrescription(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePrescription request,
            HttpServletRequest httpRequest) throws AccessDeniedException {

        return ResponseEntity.ok(service.updatePrescription(id, request, httpRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> get(
            @PathVariable Long id,
            HttpServletRequest httpRequest) throws AccessDeniedException {

        return ResponseEntity.ok(service.getPrescription(id, httpRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) throws AccessDeniedException {

        service.deletePrescription(id, httpRequest);
        return ResponseEntity.noContent().build();
    }
}
