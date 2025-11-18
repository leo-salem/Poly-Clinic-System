package polyClinicSystem.example.prescription_service.exception.customExceptions;
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
