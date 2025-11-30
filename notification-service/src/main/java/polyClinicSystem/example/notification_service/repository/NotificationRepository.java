package polyClinicSystem.example.notification_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import polyClinicSystem.example.notification_service.model.Notification;
import polyClinicSystem.example.notification_service.model.enums.NotificationStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific recipient
     */
    List<Notification> findByRecipientKeycloakIdOrderByCreatedAtDesc(String recipientKeycloakId);

    /**
     * Find notifications by status
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Find failed notifications that need retry
     */
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);

    /**
     * Find notifications by appointment ID
     */
    List<Notification> findByAppointmentId(Long appointmentId);

    /**
     * Count pending notifications for a user
     */
    long countByRecipientKeycloakIdAndStatus(String recipientKeycloakId, NotificationStatus status);

    /**
     * Find old notifications for cleanup
     */
    List<Notification> findByCreatedAtBefore(Instant cutoffDate);
}