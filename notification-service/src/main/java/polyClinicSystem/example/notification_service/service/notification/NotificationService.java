package polyClinicSystem.example.notification_service.service.notification;


import java.time.LocalDate;

public interface NotificationService {
    void sendPaymentConfirmedNotification(Long appointmentId, String patientKeycloakId,
                                          LocalDate appointmentDate, String period);

    void sendAppointmentScheduledNotification(Long appointmentId, String patientKeycloakId,
                                              String doctorKeycloakId, String nurseKeycloakId,
                                              LocalDate appointmentDate, String period, Long roomId);

    void sendAppointmentRejectedNotification(Long appointmentId, String patientKeycloakId, String reason);

    void sendAppointmentCancelledNotification(Long appointmentId, String patientKeycloakId,
                                              String doctorKeycloakId, LocalDate appointmentDate,
                                              String period, String reason);

    void sendAppointmentCompletedNotification(Long appointmentId, String patientKeycloakId,
                                              String doctorKeycloakId, LocalDate appointmentDate, String period);
}