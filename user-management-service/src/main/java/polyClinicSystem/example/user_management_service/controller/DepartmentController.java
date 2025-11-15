package polyClinicSystem.example.user_management_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polyClinicSystem.example.user_management_service.dto.request.create.CreateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.request.update.UpdateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.response.DepartmentResponse;
import polyClinicSystem.example.user_management_service.service.department.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.ok(departmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @RequestBody UpdateDepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartment(id));
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }
}
