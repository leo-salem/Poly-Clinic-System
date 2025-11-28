package polyClinicSystem.example.appointment_service.service.kafka.outboxPublisher;

import polyClinicSystem.example.appointment_service.model.event.OutboxEvent;

public interface OutboxPublisher {
    void publishPendingEvents();

    void markEventAsSent(OutboxEvent event);

    void handlePublishFailure(OutboxEvent event, Throwable ex);

    String getTopicForEventType(String eventType);
}
