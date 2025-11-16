package polyClinicSystem.example.user_management_service.service.room;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final DepartmentRepository departmentRepository;
    private final MapperSystem mapperSystem;

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        log.debug("Creating room with request: {}", request);

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + request.getDepartmentId()));

        Room room = mapperSystem.toRoom(request);

        // Set department relationship
        room.setDepartment(department);

        // Add to department's collection
        department.AddRoom(room);

        Room savedRoom = roomRepository.save(room);

        log.info("Room created successfully - id: {}, roomNumber: {}, type: {}, departmentId: {}",
                savedRoom.getId(), savedRoom.getRoomNumber(), savedRoom.getType(),
                savedRoom.getDepartment().getId());

        return mapperSystem.toRoomResponse(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        log.debug("Updating room {} with request: {}", id, request);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));

        if (request.getRoomNumber() != null) {
            room.setRoomNumber(request.getRoomNumber());
        }

        if (StringUtils.hasText(request.getType())) {
            room.setType(request.getType());
        }

        Room savedRoom = roomRepository.save(room);

        log.info("Room updated successfully: {}", id);

        return mapperSystem.toRoomResponse(savedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        log.debug("Deleting room: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));

        // Clean up bidirectional relationship
        if (room.getDepartment() != null) {
            room.getDepartment().RemoveRoom(room);
        }

        roomRepository.deleteById(id);

        log.info("Room deleted successfully: {}", id);
    }

    @Override
    public RoomResponse getRoom(Long id) {
        log.debug("Fetching room: {}", id);

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));

        return mapperSystem.toRoomResponse(room);
    }

    @Override
    public List<RoomResponse> getRoomsByDepartment(Long deptId) {
        log.debug("Fetching rooms for department: {}", deptId);

        departmentRepository.findById(deptId)
                .orElseThrow(() -> new NotFoundException("Department not found with id: " + deptId));

        List<RoomResponse> rooms = roomRepository.findAll()
                .stream()
                .filter(r -> r.getDepartment() != null && r.getDepartment().getId().equals(deptId))
                .map(mapperSystem::toRoomResponse)
                .toList();

        log.debug("Found {} rooms for department {}", rooms.size(), deptId);

        return rooms;
    }
}