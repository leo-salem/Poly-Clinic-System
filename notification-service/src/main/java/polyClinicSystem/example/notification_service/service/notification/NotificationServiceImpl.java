package polyClinicSystem.example.notification_service.service.notification;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import polyClinicSystem.example.notification_service.client.UserClient;
import polyClinicSystem.example.notification_service.dto.response.UserResponse;
import polyClinicSystem.example.notification_service.model.Notification;
import polyClinicSystem.example.notification_service.model.enums.NotificationStatus;
import polyClinicSystem.example.notification_service.model.enums.NotificationType;
import polyClinicSystem.example.notification_service.repository.NotificationRepository;
import polyClinicSystem.example.notification_service.service.email.EmailService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final UserClient userClient;

    @Override
    @Transactional
    public void sendPaymentConfirmedNotification(Long appointmentId, String patientKeycloakId,
                                                 LocalDate appointmentDate, String period) {
        log.debug("Sending payment confirmed notification for appointment: {}", appointmentId);

        try {
            UserResponse user = userClient.getUserByKeycloakId(patientKeycloakId);

            String subject = "Payment Confirmed - Appointment Pending Approval";
            String message = buildPaymentConfirmedMessage(user.getName(), appointmentDate, period);

            Notification notification = createNotification(
                    patientKeycloakId,
                    user.getEmail(),
                    user.getPhone(),
                    NotificationType.EMAIL,
                    subject,
                    message,
                    appointmentId,
                    "appointment.payment.created"
            );

            sendNotification(notification);

        } catch (Exception e) {
            log.error("Failed to send payment confirmed notification", e);
        }
    }

    @Override
    @Transactional
    public void sendAppointmentScheduledNotification(Long appointmentId, String patientKeycloakId,
                                                     String doctorKeycloakId, String nurseKeycloakId,
                                                     LocalDate appointmentDate, String period, Long roomId) {
        log.debug("Sending appointment scheduled notification for appointment: {}", appointmentId);

        try {
            // Notify patient
            UserResponse patient = userClient.getUserByKeycloakId(patientKeycloakId);
            UserResponse doctor = userClient.getUserByKeycloakId(doctorKeycloakId);

            String patientSubject = "Appointment Confirmed";
            String patientMessage = buildAppointmentScheduledMessageForPatient(
                    patient.getName(), doctor.getName(), appointmentDate, period, roomId
            );

            Notification patientNotification = createNotification(
                    patientKeycloakId,
                    patient.getEmail(),
                    patient.getPhone(),
                    NotificationType.EMAIL,
                    patientSubject,
                    patientMessage,
                    appointmentId,
                    "appointment.scheduled"
            );

            sendNotification(patientNotification);

            // Notify doctor
            String doctorSubject = "New Appointment Scheduled";
            String doctorMessage = buildAppointmentScheduledMessageForDoctor(
                    doctor.getName(), patient.getName(), appointmentDate, period, roomId
            );

            Notification doctorNotification = createNotification(
                    doctorKeycloakId,
                    doctor.getEmail(),
                    doctor.getPhone(),
                    NotificationType.EMAIL,
                    doctorSubject,
                    doctorMessage,
                    appointmentId,
                    "appointment.scheduled"
            );

            sendNotification(doctorNotification);

            // Notify nurse if assigned
            if (nurseKeycloakId != null) {
                UserResponse nurse = userClient.getUserByKeycloakId(nurseKeycloakId);
                String nurseSubject = "New Appointment Assignment";
                String nurseMessage = buildAppointmentScheduledMessageForNurse(
                        nurse.getName(), patient.getName(), doctor.getName(), appointmentDate, period, roomId
                );

                Notification nurseNotification = createNotification(
                        nurseKeycloakId,
                        nurse.getEmail(),
                        nurse.getPhone(),
                        NotificationType.EMAIL,
                        nurseSubject,
                        nurseMessage,
                        appointmentId,
                        "appointment.scheduled"
                );

                sendNotification(nurseNotification);
            }

        } catch (Exception e) {
            log.error("Failed to send appointment scheduled notifications", e);
        }
    }

    @Override
    @Transactional
    public void sendAppointmentRejectedNotification(Long appointmentId, String patientKeycloakId, String reason) {
        log.debug("Sending appointment rejected notification for appointment: {}", appointmentId);

        try {
            UserResponse user = userClient.getUserByKeycloakId(patientKeycloakId);

            String subject = "Appointment Rejected";
            String message = buildAppointmentRejectedMessage(user.getName(), reason);

            Notification notification = createNotification(
                    patientKeycloakId,
                    user.getEmail(),
                    user.getPhone(),
                    NotificationType.EMAIL,
                    subject,
                    message,
                    appointmentId,
                    "appointment.rejected"
            );

            sendNotification(notification);

        } catch (Exception e) {
            log.error("Failed to send appointment rejected notification", e);
        }
    }

    @Override
    @Transactional
    public void sendAppointmentCancelledNotification(Long appointmentId, String patientKeycloakId,
                                                     String doctorKeycloakId, LocalDate appointmentDate,
                                                     String period, String reason) {
        log.debug("Sending appointment cancelled notification for appointment: {}", appointmentId);

        try {
            // Notify patient
            UserResponse patient = userClient.getUserByKeycloakId(patientKeycloakId);

            String patientSubject = "Appointment Cancelled";
            String patientMessage = buildAppointmentCancelledMessage(patient.getName(), appointmentDate, period, reason);

            Notification patientNotification = createNotification(
                    patientKeycloakId,
                    patient.getEmail(),
                    patient.getPhone(),
                    NotificationType.EMAIL,
                    patientSubject,
                    patientMessage,
                    appointmentId,
                    "appointment.cancelled"
            );

            sendNotification(patientNotification);

            // Notify doctor
            UserResponse doctor = userClient.getUserByKeycloakId(doctorKeycloakId);

            String doctorSubject = "Appointment Cancelled";
            String doctorMessage = buildAppointmentCancelledMessageForDoctor(
                    doctor.getName(), patient.getName(), appointmentDate, period, reason
            );

            Notification doctorNotification = createNotification(
                    doctorKeycloakId,
                    doctor.getEmail(),
                    doctor.getPhone(),
                    NotificationType.EMAIL,
                    doctorSubject,
                    doctorMessage,
                    appointmentId,
                    "appointment.cancelled"
            );

            sendNotification(doctorNotification);

        } catch (Exception e) {
            log.error("Failed to send appointment cancelled notifications", e);
        }
    }

    @Override
    @Transactional
    public void sendAppointmentCompletedNotification(Long appointmentId, String patientKeycloakId,
                                                     String doctorKeycloakId, LocalDate appointmentDate, String period) {
        log.debug("Sending appointment completed notification for appointment: {}", appointmentId);

        try {
            UserResponse patient = userClient.getUserByKeycloakId(patientKeycloakId);
            UserResponse doctor = userClient.getUserByKeycloakId(doctorKeycloakId);

            String subject = "Appointment Completed - Thank You";
            String message = buildAppointmentCompletedMessage(patient.getName(), doctor.getName(), appointmentDate, period);

            Notification notification = createNotification(
                    patientKeycloakId,
                    patient.getEmail(),
                    patient.getPhone(),
                    NotificationType.EMAIL,
                    subject,
                    message,
                    appointmentId,
                    "appointment.completed"
            );

            sendNotification(notification);

        } catch (Exception e) {
            log.error("Failed to send appointment completed notification", e);
        }
    }

    // ================ Private Helper Methods ================

    private Notification createNotification(String recipientKeycloakId, String email, String phone,
                                            NotificationType type, String subject, String message,
                                            Long appointmentId, String eventType) {
        return Notification.builder()
                .recipientKeycloakId(recipientKeycloakId)
                .recipientEmail(email)
                .recipientPhone(phone)
                .type(type)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .appointmentId(appointmentId)
                .eventType(eventType)
                .retryCount(0)
                .build();
    }

    private void sendNotification(Notification notification) {
        try {
            if (notification.getType() == NotificationType.EMAIL) {
                emailService.sendEmail(
                        notification.getRecipientEmail(),
                        notification.getSubject(),
                        notification.getMessage()
                );
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);

            log.info("Notification sent successfully: {}", notification.getId());

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);

            log.error("Failed to send notification: {}", notification.getId(), e);
        }
    }

    // ================ Message Builders ================

    private String buildPaymentConfirmedMessage(String patientName, LocalDate date, String period) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your payment has been confirmed successfully!\n\n" +
                        "Appointment Details:\n" +
                        "Date: %s\n" +
                        "Time: %s\n\n" +
                        "Your appointment is now pending admin approval. You will receive another notification once it's confirmed.\n\n" +
                        "Thank you for choosing our clinic.\n\n" +
                        "Best regards,\n" +
                        "Polyclinic System",
                patientName, date.format(DateTimeFormatter.ISO_LOCAL_DATE), period
        );
    }

    private String buildAppointmentScheduledMessageForPatient(String patientName, String doctorName,
                                                              LocalDate date, String period, Long roomId) {
        return String.format(
                "Dear %s,\n\n" +
                        "Great news! Your appointment has been confirmed.\n\n" +
                        "Appointment Details:\n" +
                        "Doctor: Dr. %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Room: %d\n\n" +
                        "Please arrive 15 minutes before your appointment time.\n\n" +
                        "Best regards,\n" +
                        "Polyclinic System",
                patientName, doctorName, date.format(DateTimeFormatter.ISO_LOCAL_DATE), period, roomId
        );
    }

    private String buildAppointmentScheduledMessageForDoctor(String doctorName, String patientName,
                                                             LocalDate date, String period, Long roomId) {
        return String.format(
                "Dear Dr. %s,\n\n" +
                        "A new appointment has been scheduled.\n\n" +
                        "Appointment Details:\n" +
                        "Patient: %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Room: %d\n\n" +
                        "Best regards,\n" +
                        "Polyclinic System",
                doctorName, patientName, date.format(DateTimeFormatter.ISO_LOCAL_DATE), period, roomId
        );
    }

    private String buildAppointmentScheduledMessageForNurse(String nurseName, String patientName,
                                                            String doctorName, LocalDate date,
                                                            String period, Long roomId) {
        return String.format(
                "Dear %s,\n\n" +
                        "You have been assigned to a new appointment.\n\n" +
                        "Appointment Details:\n" +
                        "Patient: %s\n" +
                        "Doctor: Dr. %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Room: %d\n\n" +
                        "Best regards,\n" +
                        "Polyclinic System",
                nurseName, patientName, doctorName, date.format(DateTimeFormatter.ISO_LOCAL_DATE), period, roomId
        );
    }

    private String buildAppointmentRejectedMessage(String patientName, String reason) {
        return String.format(
                "Dear %s,\n\n" +
                        "We regret to inform you that your appointment has been rejected.\n\n" +
                        "Reason: %s\n\n" +
                        "Your payment has been refunded to your original payment method.\n\n" +
                        "Please contact us if you have any questions.\n\n" +
                        "Best regards,\n" +
                        "Polyclinic System",
                patientName, reason
        );
    }

    private String buildAppointmentCancelledMessage(String patientName, LocalDate date, String period, String reason) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your appointment has been cancelled.\n\n" +
                        "Appointment Details:\n" +
                        "Date: %s\n" +
                        "Time: %s\n\n" +
                        "Reason: %s\n\n" +
                        "Your payment has been refunded to your original payment method.\n\n" +
                        "We apologize for any inconvenience.\n\n" +
                        "Best regards,\n" +
                        "Polyclinic System",
                patientName, date.format(DateTimeFormatter.ISO_LOCAL_DATE), period, reason
        );
    }

    private String buildAppointmentCancelledMessageForDoctor(String doctorName, String patientName,
                                                             LocalDate date, String period, String reason) {
        return String.format(
                "Dear Dr. %s,\n\n" +
                        "An appointment has been cancelled.\n\n" +
                        "Appointment Details:\n" +
                        "Patient: %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n\n" +
                        "Reason: %s\n\n" +
                        "Best regards,\n" +
                        "Polyclinic System",
                doctorName, patientName, date.format(DateTimeFormatter.ISO_LOCAL_DATE), period, reason
        );
    }

    private String buildAppointmentCompletedMessage(String patientName, String doctorName,
                                                    LocalDate date, String period) {
        return String.format(
                "Dear %s,\n\n" +
                        "Thank you for visiting our clinic!\n\n" +
                        "Appointment Details:\n" +
                        "Doctor: Dr. %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n\n" +
                        "We hope you had a positive experience. Please feel free to book another appointment anytime.\n\n" +
                        "Best regards,\n" +
                        "Polyclinic System",
                patientName, doctorName, date.format(DateTimeFormatter.ISO_LOCAL_DATE), period
        );
    }
}