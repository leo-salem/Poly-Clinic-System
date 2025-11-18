package polyClinicSystem.example.user_management_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import polyClinicSystem.example.user_management_service.dto.request.create.CreateDepartmentRequest;
import polyClinicSystem.example.user_management_service.dto.request.create.CreateRoomRequest;
import polyClinicSystem.example.user_management_service.dto.request.create.RegisterStaffRequest;
import polyClinicSystem.example.user_management_service.dto.response.DepartmentResponse;
import polyClinicSystem.example.user_management_service.dto.response.RoomResponse;
import polyClinicSystem.example.user_management_service.dto.response.UserResponse;
import polyClinicSystem.example.user_management_service.model.department.Department;
import polyClinicSystem.example.user_management_service.model.department.Room;
import polyClinicSystem.example.user_management_service.model.user.Doctor;
import polyClinicSystem.example.user_management_service.model.user.Nurse;
import polyClinicSystem.example.user_management_service.model.user.Patient;
import polyClinicSystem.example.user_management_service.model.user.User;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MapperSystem {


     Patient toPatient(RegisterStaffRequest request);
     UserResponse toUserResponse(Patient patient);
     UserResponse toUserResponse(Doctor doctor);
     UserResponse toUserResponse(User user);
     Doctor toDoctor(RegisterStaffRequest request);
     UserResponse toUserResponse(Nurse nurse);
     Nurse toNurse(RegisterStaffRequest request);
     @Mapping(target = "department", ignore = true)
     @Mapping(target = "id", ignore = true)
     Room toRoom(CreateRoomRequest request);
     @Mapping(source = "department.id", target = "departmentId")
     RoomResponse toRoomResponse(Room room);
     Department toDepartment(CreateDepartmentRequest request);
     DepartmentResponse toDepartmentResponse(Department department);

}
