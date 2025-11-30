package polyClinicSystem.example.appointment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import polyClinicSystem.example.appointment_service.model.entity.unavailability.VacationUnavailability;

import java.util.List;

@Repository
public interface VacationUnavailabilityRepository extends JpaRepository<VacationUnavailability, Long> {

    /**
     * Find all vacation unavailabilities for a doctor
     * Used to check if doctor is on vacation before creating appointment
     */
    List<VacationUnavailability> findByDoctorKeycloakId(String doctorKeycloakId);

    /**
     * Check if doctor has vacation on specific day of week
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END " +
            "FROM VacationUnavailability v JOIN v.daysOfWeek d " +
            "WHERE v.doctorKeycloakId = :doctorKeycloakId " +
            "AND d = :dayOfWeek")
    boolean existsByDoctorAndDayOfWeek(
            String doctorKeycloakId,
            String dayOfWeek
    );
}