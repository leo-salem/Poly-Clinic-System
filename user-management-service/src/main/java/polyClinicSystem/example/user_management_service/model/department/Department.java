package polyClinicSystem.example.user_management_service.model.department;


import jakarta.persistence.*;
import lombok.*;
import polyClinicSystem.example.user_management_service.model.user.Doctor;
import polyClinicSystem.example.user_management_service.model.user.Nurse;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @OneToMany(mappedBy = "department",
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Room> rooms;

    @OneToMany(mappedBy = "department",fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Doctor> doctors;

    @OneToMany(mappedBy = "department",fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Nurse> nurses;

    public void AddRoom(Room room){
        if(rooms == null){
            rooms = new HashSet<>();
        }
        rooms.add(room);
        room.setDepartment(this);
    }

    public void RemoveRoom(Room room){
        if(rooms != null){
            rooms.remove(room);
            room.setDepartment(null);
        }
    }

    public void AddDoctor(Doctor doctor){
        if(doctors == null){
            doctors = new HashSet<>();
        }
        doctors.add(doctor);
        doctor.setDepartment(this);
    }

    public void RemoveDoctor(Doctor doctor){
        if(doctors != null){
            doctors.remove(doctor);
            doctor.setDepartment(null);
        }
    }

    public void AddNurse(Nurse nurse){
        if(nurses == null){
            nurses = new HashSet<>();
        }
        nurses.add(nurse);
        nurse.setDepartment(this);
    }

    public void RemoveNurse(Nurse nurse){
        if(nurses != null){
            nurses.remove(nurse);
            nurse.setDepartment(null);
        }
    }
}
