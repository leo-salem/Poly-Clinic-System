package polyClinicSystem.example.user_management_service.service.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import polyClinicSystem.example.user_management_service.dto.request.create.CreateRoomRequest;
import polyClinicSystem.example.user_management_service.dto.request.update.UpdateRoomRequest;
import polyClinicSystem.example.user_management_service.dto.response.RoomResponse;
import polyClinicSystem.example.user_management_service.exception.customExceptions.NotFoundException;
import polyClinicSystem.example.user_management_service.mapper.MapperSystem;
import polyClinicSystem.example.user_management_service.model.department.Department;
import polyClinicSystem.example.user_management_service.model.department.Room;
import polyClinicSystem.example.user_management_service.repository.DepartmentRepository;
import polyClinicSystem.example.user_management_service.repository.RoomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final DepartmentRepository departmentRepository;
    private final MapperSystem mapperSystem;

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Department not found"));

        Room room = mapperSystem.toRoom(request);
        room.setDepartment(department);
        department.getRooms().add(room);
        roomRepository.save(room);

        return mapperSystem.toRoomResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        if (request.getRoomNumber() != null)
            room.setRoomNumber(request.getRoomNumber());

        if (StringUtils.hasText(request.getType()))
            room.setType(request.getType());

        roomRepository.save(room);

        return mapperSystem.toRoomResponse(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    @Override
    public RoomResponse getRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        return mapperSystem.toRoomResponse(room);
    }

    @Override
    public List<RoomResponse> getRoomsByDepartment(Long deptId) {
        return roomRepository.findAll()
                .stream()
                .filter(r -> r.getDepartment() != null && r.getDepartment().getId().equals(deptId))
                .map(mapperSystem::toRoomResponse)
                .toList();
    }

}
