package polyClinicSystem.example.user_management_service.exception.customExceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}