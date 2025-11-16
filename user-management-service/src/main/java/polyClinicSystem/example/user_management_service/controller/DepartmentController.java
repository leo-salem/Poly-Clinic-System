package polyClinicSystem.example.user_management_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polyClinicSystem.example.user_management_service.dto.request.create.CreateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.request.update.UpdateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.response.DepartmentResponse;
import polyClinicSystem.example.user_management_service.dto.response.RoomResponse;
import polyClinicSystem.example.user_management_service.dto.response.UserResponse;
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

    @GetMapping("/{id}/doctors")
    public ResponseEntity<List<UserResponse>> getDoctorsByDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDoctorsByDepartment(id));
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<List<RoomResponse>> getRoomsByDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getRoomsByDepartment(id));
    }

    @GetMapping("/{id}/nurses")
    public ResponseEntity<List<UserResponse>> getNursesByDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getNursesByDepartment(id));
    }

    //  Get all staff (doctors + nurses) in a department
    @GetMapping("/{id}/staff")
    public ResponseEntity<List<UserResponse>> getAllStaffByDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getAllStaffByDepartment(id));
    }
}