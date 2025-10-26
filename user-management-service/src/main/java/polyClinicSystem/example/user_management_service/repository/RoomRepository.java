package polyClinicSystem.example.user_management_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import polyClinicSystem.example.user_management_service.model.department.Room;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByDepartmentId(Long departmentId);
}
