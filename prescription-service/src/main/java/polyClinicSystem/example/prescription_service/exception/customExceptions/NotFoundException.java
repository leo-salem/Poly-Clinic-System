package polyClinicSystem.example.prescription_service.exception.customExceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
