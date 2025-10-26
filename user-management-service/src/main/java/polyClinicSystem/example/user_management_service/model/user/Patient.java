package polyClinicSystem.example.user_management_service.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import polyClinicSystem.example.user_management_service.model.enums.BloodType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patients")
public class Patient extends User {

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;
}
