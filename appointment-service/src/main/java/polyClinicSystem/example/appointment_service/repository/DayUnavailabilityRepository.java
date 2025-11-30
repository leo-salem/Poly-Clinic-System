package polyClinicSystem.example.appointment_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import polyClinicSystem.example.appointment_service.model.entity.unavailability.DayUnavailability;
import polyClinicSystem.example.appointment_service.model.enums.Period;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DayUnavailabilityRepository extends JpaRepository<DayUnavailability, Long> {

    /**
     * Find all day unavailabilities for a doctor on a specific date
     * Used to check if doctor is unavailable before creating appointment
     */
    List<DayUnavailability> findByDoctorKeycloakIdAndAppointmentDate(
            String doctorKeycloakId,
            LocalDate appointmentDate
    );

    /**
     * Check if doctor has unavailability for specific date and period
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
            "FROM DayUnavailability d JOIN d.periods p " +
            "WHERE d.doctorKeycloakId = :doctorKeycloakId " +
            "AND d.appointmentDate = :date " +
            "AND p = :period")
    boolean existsByDoctorAndDateAndPeriod(
            String doctorKeycloakId,
            LocalDate date,
            Period period
    );
}