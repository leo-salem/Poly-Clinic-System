package polyClinicSystem.example.notification_service.service.email;

public interface EmailService {
    void sendEmail(String to, String subject, String message);
}