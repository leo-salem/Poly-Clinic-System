package polyClinicSystem.example.notification_service.service.kafkaConsumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import polyClinicSystem.example.notification_service.dto.event.*;
import polyClinicSystem.example.notification_service.service.notification.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.appointment-payment-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentConfirmedEvent(PaymentConfirmedEvent event, Acknowledgment ack) {
        log.info("Received PaymentConfirmedEvent for appointment: {}", event.getAppointmentId());

        try {
            notificationService.sendPaymentConfirmedNotification(
                    event.getAppointmentId(),
                    event.getPatientKeycloakId(),
                    event.getAppointmentDate(),
                    event.getPeriod()
            );

            ack.acknowledge();
            log.info("Successfully processed PaymentConfirmedEvent for appointment: {}", event.getAppointmentId());

        } catch (Exception e) {
            log.error("Failed to process PaymentConfirmedEvent for appointment: {}", event.getAppointmentId(), e);
            // Don't acknowledge - message will be retried
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.appointment-scheduled}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAppointmentScheduledEvent(AppointmentScheduledEvent event, Acknowledgment ack) {
        log.info("Received AppointmentScheduledEvent for appointment: {}", event.getAppointmentId());

        try {
            notificationService.sendAppointmentScheduledNotification(
                    event.getAppointmentId(),
                    event.getPatientKeycloakId(),
                    event.getDoctorKeycloakId(),
                    event.getNurseKeycloakId(),
                    event.getAppointmentDate(),
                    event.getPeriod(),
                    event.getRoomId()
            );

            ack.acknowledge();
            log.info("Successfully processed AppointmentScheduledEvent for appointment: {}", event.getAppointmentId());

        } catch (Exception e) {
            log.error("Failed to process AppointmentScheduledEvent for appointment: {}", event.getAppointmentId(), e);
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.appointment-rejected}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAppointmentRejectedEvent(AppointmentRejectedEvent event, Acknowledgment ack) {
        log.info("Received AppointmentRejectedEvent for appointment: {}", event.getAppointmentId());

        try {
            notificationService.sendAppointmentRejectedNotification(
                    event.getAppointmentId(),
                    event.getPatientKeycloakId(),
                    event.getRejectionReason()
            );

            ack.acknowledge();
            log.info("Successfully processed AppointmentRejectedEvent for appointment: {}", event.getAppointmentId());

        } catch (Exception e) {
            log.error("Failed to process AppointmentRejectedEvent for appointment: {}", event.getAppointmentId(), e);
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.appointment-cancelled}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAppointmentCancelledEvent(AppointmentCancelledEvent event, Acknowledgment ack) {
        log.info("Received AppointmentCancelledEvent for appointment: {}", event.getAppointmentId());

        try {
            notificationService.sendAppointmentCancelledNotification(
                    event.getAppointmentId(),
                    event.getPatientKeycloakId(),
                    event.getDoctorKeycloakId(),
                    event.getAppointmentDate(),
                    event.getPeriod(),
                    event.getCancellationReason()
            );

            ack.acknowledge();
            log.info("Successfully processed AppointmentCancelledEvent for appointment: {}", event.getAppointmentId());

        } catch (Exception e) {
            log.error("Failed to process AppointmentCancelledEvent for appointment: {}", event.getAppointmentId(), e);
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.appointment-completed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAppointmentCompletedEvent(AppointmentCompletedEvent event, Acknowledgment ack) {
        log.info("Received AppointmentCompletedEvent for appointment: {}", event.getAppointmentId());

        try {
            notificationService.sendAppointmentCompletedNotification(
                    event.getAppointmentId(),
                    event.getPatientKeycloakId(),
                    event.getDoctorKeycloakId(),
                    event.getAppointmentDate(),
                    event.getPeriod()
            );

            ack.acknowledge();
            log.info("Successfully processed AppointmentCompletedEvent for appointment: {}", event.getAppointmentId());

        } catch (Exception e) {
            log.error("Failed to process AppointmentCompletedEvent for appointment: {}", event.getAppointmentId(), e);
        }
    }
}