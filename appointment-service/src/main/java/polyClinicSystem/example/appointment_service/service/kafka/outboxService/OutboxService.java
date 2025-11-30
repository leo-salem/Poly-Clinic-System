package polyClinicSystem.example.appointment_service.service.kafka.outboxService;

import polyClinicSystem.example.appointment_service.model.entity.Appointment;

public interface OutboxService {
    void publishPaymentConfirmedEvent(Appointment appointment);
    void publishAppointmentScheduledEvent(Appointment appointment);
    void publishAppointmentRejectedEvent(Appointment appointment, String reason);
    void publishAppointmentCancelledEvent(Appointment appointment, String cancellationReason);
    void publishAppointmentCompletedEvent(Appointment appointment);
}