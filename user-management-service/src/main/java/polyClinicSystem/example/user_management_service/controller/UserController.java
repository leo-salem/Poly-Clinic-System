package polyClinicSystem.example.user_management_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polyClinicSystem.example.user_management_service.dto.request.create.RegisterStaffRequest;
import polyClinicSystem.example.user_management_service.dto.response.UserResponse;
import polyClinicSystem.example.user_management_service.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/patients")
    public ResponseEntity<UserResponse> registerPatient(@RequestBody RegisterStaffRequest request) {
        return ResponseEntity.ok(userService.registerPatient(request));
    }

    //  Staff (Doctor / Nurse)
    @PostMapping("/staff")
    public ResponseEntity<UserResponse> addStaff(@RequestBody RegisterStaffRequest request) {
        return ResponseEntity.ok(userService.addStaff(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody RegisterStaffRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // -------- Change Password --------
    @PutMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            @RequestParam(required = false) String oldPassword) {

        userService.ChangePassword(id, oldPassword, newPassword);
        return ResponseEntity.noContent().build();
    }
}
