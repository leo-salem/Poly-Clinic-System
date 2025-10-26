package polyClinicSystem.example.prescription_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import polyClinicSystem.example.prescription_service.model.Prescription;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByMedicalRecordId(Long medicalRecordId);
}
