package polyClinicSystem.example.user_management_service.exception.customExceptions;

public class KeycloakServerException extends RuntimeException {
    public KeycloakServerException(String message) {
        super(message);
    }
}
