package polyClinicSystem.example.user_management_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import polyClinicSystem.example.user_management_service.dto.request.create.CreateRoomRequest;
import polyClinicSystem.example.user_management_service.dto.request.update.UpdateRoomRequest;
import polyClinicSystem.example.user_management_service.dto.response.RoomResponse;
import polyClinicSystem.example.user_management_service.service.room.RoomService;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody CreateRoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @RequestBody UpdateRoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoom(id));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(roomService.getRoomsByDepartment(deptId));
    }
}
