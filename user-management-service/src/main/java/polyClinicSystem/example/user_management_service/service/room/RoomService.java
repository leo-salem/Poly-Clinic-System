package polyClinicSystem.example.user_management_service.service.room;

import polyClinicSystem.example.user_management_service.dto.request.create.CreateRoomRequest;
import polyClinicSystem.example.user_management_service.dto.request.update.UpdateRoomRequest;
import polyClinicSystem.example.user_management_service.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {
    RoomResponse createRoom(CreateRoomRequest request);
    RoomResponse updateRoom(Long id, UpdateRoomRequest request);
    void deleteRoom(Long id);
    RoomResponse getRoom(Long id);
    List<RoomResponse> getRoomsByDepartment(Long deptId);
}
