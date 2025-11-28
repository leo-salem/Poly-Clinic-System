package polyClinicSystem.example.appointment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import polyClinicSystem.example.appointment_service.model.entity.Appointment;
import polyClinicSystem.example.appointment_service.model.enums.Period;
import polyClinicSystem.example.appointment_service.model.enums.Status;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

import static jakarta.persistence.ParameterMode.IN;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    /**
     * Find an appointment by its unique reservation token.
     * Used during payment confirmation step.
     * @param reservationToken The UUID token returned during reservation
     * @return Optional containing appointment if found
     */
    Optional<Appointment> findByReservationToken(String reservationToken);

    /**
     * Find all appointments for a specific doctor on a specific date.
     * Used to calculate available time slots.
     * @return List of all appointments for that doctor/date (any status)
     */
    List<Appointment> findByDoctorKeycloakIdAndAppointmentDate(
            String doctorKeycloakId,
            LocalDate appointmentDate
    );

    /**
     * Find appointments by doctor, date, and period (time slot).
     * Used to check if a specific slot is available.
     * @return List of appointments matching criteria (should be 0 or 1 due to unique constraint)
     */
    List<Appointment> findByDoctorKeycloakIdAndAppointmentDateAndPeriod(
            String doctorKeycloakId,
            LocalDate appointmentDate,
            Period period
    );

    /**
     * Find appointments by doctor, date, period, and status IN a list.
     * Used to check if slot is available (excluding EXPIRED/CANCELLED/REJECTED).
     * @param statuses List of statuses to include (typically PENDING, PAID, SCHEDULED)
     */
    List<Appointment> findByDoctorKeycloakIdAndAppointmentDateAndPeriodAndStatusIn(
            String doctorKeycloakId,
            LocalDate appointmentDate,
            Period period,
            List<Status> statuses
    );

    /**
     * Find expired PENDING reservations for cleanup.
     * Scheduler uses this to find reservations past their TTL.
     *
     * @param status The status to filter (typically PENDING)
     * @param cutoffTime Timestamp threshold (appointments with expiresAt < these are expired)
     * @return List of expired reservations
     */
    List<Appointment> findByStatusAndExpiresAtBefore(
            Status status,
            Instant cutoffTime
    );

    /**
     * Find all appointments for a patient, ordered by date (newest first).
     * Used for "My Appointments" patient view.
     *
     * @param patientKeycloakId Patient's Keycloak ID
     * @return List of patient's appointments
     */
    List<Appointment> findByPatientKeycloakIdOrderByAppointmentDateDesc(
            String patientKeycloakId
    );

    /**
     * Find all appointments for a doctor, ordered by date (newest first).
     * Used for doctor's appointment dashboard.
     *
     * @param doctorKeycloakId Doctor's Keycloak ID
     * @return List of doctor's appointments
     */
    List<Appointment> findByDoctorKeycloakIdOrderByAppointmentDateDesc(
            String doctorKeycloakId
    );

    /**
     * Find all appointments for a nurse, ordered by date (newest first).
     * Used for nurse's appointment dashboard.
     *
     * @param nurseKeycloakId Nurse's Keycloak ID
     * @return List of nurse's appointments
     */
    List<Appointment> findByNurseKeycloakIdOrderByAppointmentDateDesc(
            String nurseKeycloakId
    );

    /**
     * Find all appointments with a specific status, ordered by date (ascending).
     * Used by admin to see pending approvals or scheduled appointments.
     * @return List of appointments with that status
     */
    List<Appointment> findByStatusOrderByAppointmentDateAsc(
            Status status
    );

    /**
     * Check if a slot exists with specific criteria.
     * Returns true if at least one appointment matches (slot is booked).
     * Used before creating a reservation to check availability.
     * @param statuses List of statuses to check (PENDING, PAID, SCHEDULED)
     * @return true if slot is already booked
     */
    boolean existsByDoctorKeycloakIdAndAppointmentDateAndPeriodAndStatusIn(
            String doctorKeycloakId,
            LocalDate appointmentDate,
            Period period,
            List<Status> statuses
    );

    /**
     * Find SCHEDULED appointments for a room at a specific date and time.
     * Used to prevent room conflicts during admin approval.
     * @param status Status (typically SCHEDULED)
     * @return Optional containing conflicting appointment if found
     */
    Optional<Appointment> findByRoomIdAndAppointmentDateAndPeriodAndStatus(
            Long roomId,
            LocalDate appointmentDate,
            Period period,
            Status status
    );

    /**
     * Find SCHEDULED appointments for a nurse at a specific date and time.
     * Used to prevent nurse conflicts during admin approval.
     * @param status Status (typically SCHEDULED)
     * @return Optional containing conflicting appointment if found
     */
    Optional<Appointment> findByNurseKeycloakIdAndAppointmentDateAndPeriodAndStatus(
            String nurseKeycloakId,
            LocalDate appointmentDate,
            Period period,
            Status status
    );


    @Query("SELECT a.period FROM Appointment a " +
            "WHERE a.doctorKeycloakId = :doctorKeycloakId " +
            "AND a.appointmentDate = :date " +
            "AND a.status IN :statuses")
    List<Period> findBookedPeriods(
            String doctorKeycloakId,
            LocalDate date,
            List<Status> statuses
    );



}
