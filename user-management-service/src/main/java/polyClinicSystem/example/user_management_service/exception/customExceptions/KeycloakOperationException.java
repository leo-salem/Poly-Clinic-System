package polyClinicSystem.example.user_management_service.exception.customExceptions;

public class KeycloakOperationException extends RuntimeException {
    public KeycloakOperationException(String message) {
        super(message);
    }
}