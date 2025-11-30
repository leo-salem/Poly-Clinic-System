package polyClinicSystem.example.appointment_service.service.kafka.outboxService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import polyClinicSystem.example.appointment_service.dto.event.*;
import polyClinicSystem.example.appointment_service.model.entity.Appointment;
import polyClinicSystem.example.appointment_service.model.event.OutboxEvent;
import polyClinicSystem.example.appointment_service.repository.OutboxEventRepository;

import java.time.Instant;


@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements OutboxService{

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishPaymentConfirmedEvent(Appointment appointment) {
        log.debug("Publishing payment confirmed event for appointment: {}", appointment.getId());

        // Check for duplicate events (idempotency)
        boolean exists = outboxRepository.findByAggregateIdAndEventType(
                appointment.getReservationToken(),
                "appointment.payment.created"
        ).stream().anyMatch(e -> !e.isSent());

        if (exists) {
            log.info("Payment confirmed event already exists for token: {}", appointment.getReservationToken());
            return;
        }

        PaymentConfirmedEvent event = PaymentConfirmedEvent.builder()
                .reservationToken(appointment.getReservationToken())
                .appointmentId(appointment.getId())
                .patientKeycloakId(appointment.getPatientKeycloakId())
                .doctorKeycloakId(appointment.getDoctorKeycloakId())
                .paymentIntentId(appointment.getPaymentIntentId())
                .appointmentDate(appointment.getAppointmentDate())
                .period(appointment.getPeriod().name())
                .timestamp(Instant.now())
                .build();

        saveOutboxEvent("appointment", appointment.getReservationToken(),
                "appointment.payment.created", event);
    }

    @Transactional
    public void publishAppointmentScheduledEvent(Appointment appointment) {
        log.debug("Publishing appointment scheduled event for appointment: {}", appointment.getId());

        AppointmentScheduledEvent event = AppointmentScheduledEvent.builder()
                .appointmentId(appointment.getId())
                .doctorKeycloakId(appointment.getDoctorKeycloakId())
                .nurseKeycloakId(appointment.getNurseKeycloakId())
                .patientKeycloakId(appointment.getPatientKeycloakId())
                .departmentId(appointment.getDepartmentId())
                .roomId(appointment.getRoomId())
                .appointmentDate(appointment.getAppointmentDate())
                .period(appointment.getPeriod().name())
                .timestamp(Instant.now())
                .build();

        saveOutboxEvent("appointment", String.valueOf(appointment.getId()),
                "appointment.scheduled", event);
    }

    @Transactional
    public void publishAppointmentRejectedEvent(Appointment appointment, String reason) {
        log.debug("Publishing appointment rejected event for appointment: {}", appointment.getId());

        AppointmentRejectedEvent event = AppointmentRejectedEvent.builder()
                .appointmentId(appointment.getId())
                .reservationToken(appointment.getReservationToken())
                .patientKeycloakId(appointment.getPatientKeycloakId())
                .paymentIntentId(appointment.getPaymentIntentId())
                .rejectionReason(reason)
                .timestamp(Instant.now())
                .build();

        saveOutboxEvent("appointment", String.valueOf(appointment.getId()),
                "appointment.rejected", event);
    }

    @Transactional
    public void publishAppointmentCancelledEvent(Appointment appointment, String cancellationReason) {
        log.debug("Publishing appointment cancelled event for appointment: {}", appointment.getId());

        AppointmentCancelledEvent event = AppointmentCancelledEvent.builder()
                .appointmentId(appointment.getId())
                .reservationToken(appointment.getReservationToken())
                .doctorKeycloakId(appointment.getDoctorKeycloakId())
                .patientKeycloakId(appointment.getPatientKeycloakId())
                .paymentIntentId(appointment.getPaymentIntentId())
                .appointmentDate(appointment.getAppointmentDate())
                .period(appointment.getPeriod().name())
                .cancellationReason(cancellationReason)
                .timestamp(Instant.now())
                .build();

        saveOutboxEvent("appointment", String.valueOf(appointment.getId()),
                "appointment.cancelled", event);
    }

    @Transactional
    public void publishAppointmentCompletedEvent(Appointment appointment) {
        log.debug("Publishing appointment completed event for appointment: {}", appointment.getId());

        AppointmentCompletedEvent event = AppointmentCompletedEvent.builder()
                .appointmentId(appointment.getId())
                .doctorKeycloakId(appointment.getDoctorKeycloakId())
                .nurseKeycloakId(appointment.getNurseKeycloakId())
                .patientKeycloakId(appointment.getPatientKeycloakId())
                .departmentId(appointment.getDepartmentId())
                .roomId(appointment.getRoomId())
                .appointmentDate(appointment.getAppointmentDate())
                .period(appointment.getPeriod().name())
                .timestamp(Instant.now())
                .build();

        saveOutboxEvent("appointment", String.valueOf(appointment.getId()),
                "appointment.completed", event);
    }

    public void saveOutboxEvent(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .sent(false)
                    .createdAt(Instant.now())
                    .retryCount(0)
                    .build();

            outboxRepository.save(outboxEvent);

            log.info("Outbox event saved: type={}, aggregateId={}", eventType, aggregateId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload", e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
}