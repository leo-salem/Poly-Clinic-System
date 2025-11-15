package polyClinicSystem.example.user_management_service.exception.customExceptions;
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
