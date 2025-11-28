package polyClinicSystem.example.payment_service.exception.customExceptions;

public class NotFoundException extends RuntimeException  {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
