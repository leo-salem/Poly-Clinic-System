package polyClinicSystem.example.appointment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import polyClinicSystem.example.appointment_service.model.Appointment;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByAppointmentDateTime(LocalDateTime appointmentDateTime);
}
