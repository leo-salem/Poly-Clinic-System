package polyClinicSystem.example.appointment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import polyClinicSystem.example.appointment_service.model.event.OutboxEvent;

import java.time.Instant;
import java.util.*;

/**
 * Repository for OutboxEvent entity.
 * Manages events waiting to be published to Kafka.
 */

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Find the oldest 100 unsent events.
     * Scheduler polls this regularly to publish events to Kafka.
     * Ordered by creation time to maintain event ordering.
     *
     * @param sent Flag to filter (false = not yet sent)
     * @return List of up to 100 unsent events
     */
    List<OutboxEvent> findTop100BySentOrderByCreatedAtAsc(boolean sent);

    /**
     * Find events by aggregate ID and event type.
     * Used for idempotency check - prevent duplicate events for same action.
     *
     * @param aggregateId The entity ID (appointment ID or reservation token)
     * @param eventType The event type ("appointment.scheduled")
     * @return List of matching events
     */
    List<OutboxEvent> findByAggregateIdAndEventType(
            String aggregateId,
            String eventType
    );

    List<OutboxEvent> findTop100BySentFalseOrderByCreatedAtAsc();

    /**
     * Find old sent events for cleanup
     */
    List<OutboxEvent> findBySentTrueAndSentAtBefore(Instant cutoffTime);
}
