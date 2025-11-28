package polyClinicSystem.example.appointment_service.exception.customExceptions;


public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}