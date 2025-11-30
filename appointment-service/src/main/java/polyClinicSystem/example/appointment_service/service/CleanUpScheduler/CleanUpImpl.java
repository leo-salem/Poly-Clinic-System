package polyClinicSystem.example.appointment_service.service.CleanUpScheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import polyClinicSystem.example.appointment_service.model.entity.Appointment;
import polyClinicSystem.example.appointment_service.model.enums.Status;
import polyClinicSystem.example.appointment_service.repository.AppointmentRepository;
import polyClinicSystem.example.appointment_service.service.kafka.outboxService.OutboxService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanUpImpl implements CleanUpService {

    private final AppointmentRepository appointmentRepository;
    private final OutboxService outboxService;

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

    /**
     * Runs every day at 1:00 AM to mark past SCHEDULED appointments as COMPLETED
     */
    @Scheduled(cron = "0 0 1 * * *") // Every day at 1:00 AM
    @Transactional
    public void markCompletedAppointments() {
        log.debug("Starting marking of completed appointments");

        try {
            LocalDate today = LocalDate.now();

            // Find all SCHEDULED appointments with date before today
            List<Appointment> scheduledAppointments = appointmentRepository
                    .findByStatusOrderByAppointmentDateAsc(Status.SCHEDULED);

            List<Appointment> pastAppointments = scheduledAppointments.stream()
                    .filter(appointment -> appointment.getAppointmentDate().isBefore(today))
                    .toList();

            if (!pastAppointments.isEmpty()) {
                log.info("Found {} past scheduled appointments to mark as completed", pastAppointments.size());

                for (Appointment appointment : pastAppointments) {
                    appointment.setStatus(Status.COMPLETED);
                    Appointment saved = appointmentRepository.save(appointment);

                    // Publish completed event
                    outboxService.publishAppointmentCompletedEvent(saved);

                    log.debug("Marked appointment {} as COMPLETED", appointment.getId());
                }

                log.info("Successfully marked {} appointments as COMPLETED", pastAppointments.size());
            }

        } catch (Exception e) {
            log.error("Error during marking completed appointments", e);
        }
    }

    /**
     * Runs every day at 2:00 AM to delete old appointments
     * Deletes appointments that are CANCELLED, EXPIRED, COMPLETED, or REJECTED
     * and are older than 30 days
     */
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2:00 AM
    @Transactional
    public void deleteOldAppointments() {
        log.debug("Starting deletion of old appointments");

        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(30);

            List<Status> statusesToDelete = Arrays.asList(
                    Status.CANCELLED,
                    Status.EXPIRED,
                    Status.COMPLETED,
                    Status.REJECTED
            );

            int deletedCount = 0;

            for (Status status : statusesToDelete) {
                List<Appointment> oldAppointments = appointmentRepository
                        .findByStatusOrderByAppointmentDateAsc(status)
                        .stream()
                        .filter(appointment -> appointment.getAppointmentDate().isBefore(cutoffDate))
                        .toList();

                if (!oldAppointments.isEmpty()) {
                    log.info("Found {} old {} appointments to delete", oldAppointments.size(), status);

                    appointmentRepository.deleteAll(oldAppointments);
                    deletedCount += oldAppointments.size();

                    log.debug("Deleted {} {} appointments", oldAppointments.size(), status);
                }
            }

            if (deletedCount > 0) {
                log.info("Successfully deleted {} old appointments in total", deletedCount);
            } else {
                log.debug("No old appointments to delete");
            }

        } catch (Exception e) {
            log.error("Error during deletion of old appointments", e);
        }
    }
}