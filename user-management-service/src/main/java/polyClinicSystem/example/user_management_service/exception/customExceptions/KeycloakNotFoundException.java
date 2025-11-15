package polyClinicSystem.example.user_management_service.exception.customExceptions;

public class KeycloakNotFoundException extends RuntimeException {
    public KeycloakNotFoundException(String message) {
        super(message);
    }
}
