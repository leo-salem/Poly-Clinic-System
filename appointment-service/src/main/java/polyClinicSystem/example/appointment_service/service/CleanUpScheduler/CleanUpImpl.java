package polyClinicSystem.example.appointment_service.service.CleanUpScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import polyClinicSystem.example.appointment_service.model.entity.Appointment;
import polyClinicSystem.example.appointment_service.model.enums.Status;
import polyClinicSystem.example.appointment_service.repository.AppointmentRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanUpImpl implements CleanUpService{
    private final AppointmentRepository appointmentRepository;

    /**
     * Runs every 2 minutes to clean up expired PENDING reservations
     */
    @Scheduled(fixedDelay = 120000, initialDelay = 60000) // 2 minutes
    @Transactional
    public void cleanupExpiredReservations() {
        log.debug("Starting cleanup of expired reservations");

        try {
            Instant cutoffTime = Instant.now();

            List<Appointment> expiredReservations = appointmentRepository.findByStatusAndExpiresAtBefore(
                    Status.PENDING,
                    cutoffTime
            );

            if (!expiredReservations.isEmpty()) {
                log.info("Found {} expired reservations to clean up", expiredReservations.size());

                for (Appointment appointment : expiredReservations) {
                    appointment.setStatus(Status.EXPIRED);
                    appointmentRepository.save(appointment);

                    log.debug("Marked appointment {} as EXPIRED", appointment.getId());
                }

                log.info("Successfully cleaned up {} expired reservations", expiredReservations.size());
            }

        } catch (Exception e) {
            log.error("Error during reservation cleanup", e);
        }
    }
}
