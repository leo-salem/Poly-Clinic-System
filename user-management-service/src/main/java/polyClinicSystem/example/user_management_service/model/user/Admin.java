package polyClinicSystem.example.user_management_service.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import polyClinicSystem.example.user_management_service.model.enums.BloodType;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends User {

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    // أي حقول إضافية للـ Admin

}