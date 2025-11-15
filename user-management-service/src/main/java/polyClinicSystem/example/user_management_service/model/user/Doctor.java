package polyClinicSystem.example.user_management_service.model.user;

import jakarta.persistence.*;
import lombok.*;
import polyClinicSystem.example.user_management_service.model.department.Department;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doctors")
public class Doctor extends User {

    private String specialization;

    private int experience_years;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="department_id")
    private Department department;
}
