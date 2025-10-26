package polyClinicSystem.example.user_management_service.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import polyClinicSystem.example.user_management_service.model.user.Patient;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

}