package polyClinicSystem.example.appointment_service.exception.customExceptions;

public class AccessDeniedException extends RuntimeException  {
    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}