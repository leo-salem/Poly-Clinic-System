package polyClinicSystem.example.appointment_service.service.kafka.outboxPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import polyClinicSystem.example.appointment_service.model.event.OutboxEvent;
import polyClinicSystem.example.appointment_service.repository.OutboxEventRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherImpl implements OutboxPublisher {
    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // Every 5 seconds
    @Transactional
    public void publishPendingEvents() {
        try {
            List<OutboxEvent> pendingEvents = outboxRepository.findTop100BySentFalseOrderByCreatedAtAsc();

            if (!pendingEvents.isEmpty()) {
                log.debug("Publishing {} pending outbox events", pendingEvents.size());

                for (OutboxEvent event : pendingEvents) {
                    try {
                        // Determine Kafka topic based on event type
                        String topic = getTopicForEventType(event.getEventType());

                /**
                 * don't need this line because it's already object (it built in this service as object (dto) and sent)
                         Object payload = objectMapper.readValue(event.getPayload(), Object. Class);
                 **/
                        // Send to Kafka
                        kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload())
                                .whenComplete((result, ex) -> {
                                    if (ex == null) {
                                        markEventAsSent(event);
                                        log.debug("Event published to Kafka: id={}, topic={}", event.getId(), topic);
                                    } else {
                                        handlePublishFailure(event, ex);
                                    }
                                });

                    } catch (Exception e) {
                        log.error("Failed to publish event: id={}", event.getId(), e);
                        event.setRetryCount(event.getRetryCount() + 1);
                        outboxRepository.save(event);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error in outbox publisher", e);
        }
    }

    @Transactional
    public void markEventAsSent(OutboxEvent event) {
        event.setSent(true);
        event.setSentAt(Instant.now());
        outboxRepository.save(event);
    }

    public void handlePublishFailure(OutboxEvent event, Throwable ex) {
        log.error("Failed to publish event to Kafka: id={}", event.getId(), ex);
        event.setRetryCount(event.getRetryCount() + 1);

        // If retry count exceeds threshold, mark as failed (or implement dead letter queue)
        if (event.getRetryCount() > 10) {
            log.error("Event exceeded max retries: id={}", event.getId());
            // Optionally: move to dead letter table or alert
        }

        outboxRepository.save(event);
    }

    public String getTopicForEventType(String eventType) {
        return switch (eventType) {
            case "appointment.payment.created" -> "appointment-payment-created";
            case "appointment.scheduled" -> "appointment-scheduled";
            case "appointment.rejected" -> "appointment-rejected";
            default -> "appointment-events";
        };
    }

    /**
     * Cleanup old sent events (runs daily at 3 AM)
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldEvents() {
        log.debug("Starting cleanup of old outbox events");

        try {
            Instant cutoffTime = Instant.now().minusSeconds(30 * 24 * 60 * 60); // 30 days ago

            List<OutboxEvent> oldEvents = outboxRepository
                    .findBySentTrueAndSentAtBefore(cutoffTime);

            if (!oldEvents.isEmpty()) {
                outboxRepository.deleteAll(oldEvents);
                log.info("Cleaned up {} old outbox events", oldEvents.size());
            }

        } catch (Exception e) {
            log.error("Error during outbox cleanup", e);
        }
    }
}
